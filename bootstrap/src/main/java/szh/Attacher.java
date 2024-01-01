package szh;

import net.bytebuddy.agent.ByteBuddyAgent;

import java.io.File;

public class Attacher {
    public static void main(String[] args) {
        if (args.length < 1){
            System.out.println("Usage: java Attacher <pid>");
            System.exit(1);
        }
        String pid = args[0];
        // 用ByteBuddyAgentAttach将当前class所在的agent附加到目标pid上
        String path = Attacher.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        File agentFile = new File(path);
        System.out.println("Attaching agent: " + agentFile.getAbsolutePath());
        ByteBuddyAgent.attach(agentFile, pid, "");
        System.out.println("Attached successfully");
    }
}
