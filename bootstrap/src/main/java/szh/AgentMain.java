package szh;


import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.util.jar.JarFile;

public class AgentMain {
    static final String INITIALIZE_CLASS = "java.lang.szh.szh.Main";
    static final String INITIALIZE_METHOD = "initialize";
    public static void agentmain(String agentArgs, Instrumentation inst) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String path = AgentMain.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        path = path.substring(0, path.lastIndexOf("/"));
        path = path + "/agent-core.jar";
        System.out.println("agent path:" + path);
        File jar = new File(path);
        inst.appendToBootstrapClassLoaderSearch(new JarFile(jar));
        Class<?> aClass = Class.forName(INITIALIZE_CLASS, true, null);
        aClass.getMethod(INITIALIZE_METHOD, File.class, Instrumentation.class).invoke(null, jar, inst);
//        try {
//            AgentBootClassLoader agentBootClassLoader = new AgentBootClassLoader(jar, null);
//            Class<?> aClass = Class.forName(INITIALIZE_CLASS, true, agentBootClassLoader);
//            aClass.getMethod(INITIALIZE_METHOD, File.class, Instrumentation.class).invoke(null, jar, inst);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }
    public static void premain(String agentArgs, Instrumentation inst) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        agentmain(agentArgs, inst);
    }
}
