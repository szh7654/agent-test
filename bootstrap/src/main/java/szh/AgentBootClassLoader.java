package szh;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;

public class AgentBootClassLoader extends URLClassLoader {
    private static final ProtectionDomain PROTECTION_DOMAIN;

    private static final String START_WITH = "szh";
    private static final String CLASS_FILE_SUFFIX = ".class";

    static {
        ClassLoader.registerAsParallelCapable();

        if (System.getSecurityManager() == null) {
            PROTECTION_DOMAIN = AgentBootClassLoader.class.getProtectionDomain();
        } else {
            PROTECTION_DOMAIN = AccessController.doPrivileged(new PrivilegedAction<ProtectionDomain>() {
                @Override
                public ProtectionDomain run() {
                    return AgentBootClassLoader.class.getProtectionDomain();
                }
            });
        }
    }

    URL jarUrl;

    public AgentBootClassLoader(File jar, ClassLoader parent) throws MalformedURLException {
        super(new URL[]{jar.toURI().toURL()}, parent);
        this.jarUrl = jar.toURI().toURL();

    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (!name.startsWith(START_WITH)) {
            return super.loadClass(name, resolve);
        }
        synchronized (getClassLoadingLock(name)) {
            try {
                // First, check if the class has already been loaded
                Class<?> c = findLoadedClass(name);
                if (c == null) {
                    byte[] classBytes = getClassBytes(name);
                    if (classBytes != null) {
                        return defineClass(name, classBytes, 0, classBytes.length);
                    }
                    if (resolve) {
                        resolveClass(c);
                    }
                }
                return c;
            } catch (ClassNotFoundException e) {
                return super.loadClass(name, resolve);
            }
        }
    }

    private byte[] getClassBytes(String name) throws ClassNotFoundException {
        try (InputStream is = getPrivilegedResourceAsStream(name.replace('.', '/') + CLASS_FILE_SUFFIX)) {
            if (is != null) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int n;
                byte[] data = new byte[1024];
                while ((n = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, n);
                }
                return buffer.toByteArray();
            }
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        }
        return null;
    }

    private InputStream getPrivilegedResourceAsStream(final String name) {
        if (System.getSecurityManager() == null) {
            return getResourceAsStreamInternal(name);
        }

        return AccessController.doPrivileged(new PrivilegedAction<InputStream>() {
            @Override
            public InputStream run() {
                return getResourceAsStreamInternal(name);
            }
        });
    }

    private InputStream getResourceAsStreamInternal(String name) {
        return super.getResourceAsStream(name);
    }
}
