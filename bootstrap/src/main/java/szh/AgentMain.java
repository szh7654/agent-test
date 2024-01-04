package szh;


import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.util.jar.JarFile;


/**
 *
 * BootStrapClassLoader<===agent-core.jar
 * |
 * |
 * Initializer.class===> bytebuddy.advice()
 *                           |
 *                           |
 * AgentBootClassLoader<===agent-core.jar(interceptor.class)
 */
public class AgentMain {
    static final String INITIALIZE_CLASS = "com.unionpay.upagent.initialize.Initializer";
    static final String INITIALIZE_METHOD = "initialize";
    public static void agentmain(String agentArgs, Instrumentation inst) throws Exception {
        String path = AgentMain.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        path = path.substring(0, path.lastIndexOf("/"));
        path = path + "/agent-core.jar";
        System.out.println("agent path:" + path);
        File jar = new File(path);
        inst.appendToBootstrapClassLoaderSearch(new JarFile(jar));

        Class<?> aClass = Class.forName(INITIALIZE_CLASS, true, null);
        aClass.getMethod(INITIALIZE_METHOD, File.class, String.class, Instrumentation.class).invoke(null, jar, agentArgs == null ? "" : agentArgs, inst);
    }
    public static void premain(String agentArgs, Instrumentation inst) throws Exception {
        agentmain(agentArgs, inst);
    }
}
