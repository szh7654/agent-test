package szh;

import net.bytebuddy.dynamic.ClassFileLocator;

import java.io.IOException;

public class RootPackageCustomLocator implements ClassFileLocator {
    private final String rootPackage;
    private final ClassFileLocator classFileLocator;

    public RootPackageCustomLocator(String rootPackage, ClassFileLocator classFileLocator) {
        this.rootPackage = rootPackage;
        this.classFileLocator = classFileLocator;
    }

    /**
     * {@inheritDoc}
     */
    public Resolution locate(String name) throws IOException {
        return name.startsWith(rootPackage) ? classFileLocator.locate(name) : new Resolution.Illegal(name);
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws IOException {
        classFileLocator.close();
    }
}
