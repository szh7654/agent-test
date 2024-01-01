package szh;

import jdk.internal.agent.Agent;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

public class AgentMain {
    static final String INITIALIZE_CLASS = "szh.Main";
    static final String INITIALIZE_METHOD = "initialize";
    public static void agentmain(String agentArgs, Instrumentation inst) {
        String path = AgentMain.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        path = path.substring(0, path.lastIndexOf("/"));
        path = path + "/agent-core.jar";
        System.out.println("agent path:" + path);
        File jar = new File(path);

        try {
            AgentBootClassLoader agentBootClassLoader = new AgentBootClassLoader(jar, null);
            Class<?> aClass = Class.forName(INITIALIZE_CLASS, true, agentBootClassLoader);
            aClass.getDeclaredMethod(INITIALIZE_METHOD).invoke(null, jar);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static void premain(String agentArgs, Instrumentation inst) {
        agentmain(agentArgs, inst);
    }
}
