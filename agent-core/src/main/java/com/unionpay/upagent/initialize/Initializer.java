package com.unionpay.upagent.initialize;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.skywalking.apm.agent.core.boot.ServiceManager;
import org.apache.skywalking.apm.agent.core.conf.Config;
import org.apache.skywalking.apm.agent.core.conf.SnifferConfigInitializer;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static net.bytebuddy.matcher.ElementMatchers.*;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class Initializer {
    private static ILog LOGGER = LogManager.getLogger(Initializer.class);
    public static void initialize(File jarFile, String agentOptions, Instrumentation inst) {
        String jarPath = jarFile.getPath();
        String fileSeparator = System.getProperty("file.separator");
        if (fileSeparator == null || fileSeparator.length() == 0) {
            LOGGER.warn("file.separator is empty, use '/' instead.");
            fileSeparator = "/";
        }
        String dir = jarPath.substring(0, jarPath.lastIndexOf(fileSeparator)) + fileSeparator;
        LOGGER.info("Agent jar directory: {}", dir);
        System.setProperty("skywalking_config", dir + "agent.config");
        try {
            SnifferConfigInitializer.initializeCoreConfig(agentOptions);
        } catch (Exception e) {
            LogManager.getLogger(Initializer.class).error(e, "Agent initialized failure. Shutting down.");
            return;
        } finally {
            LOGGER = LogManager.getLogger(Initializer.class);
        }
        if (!Config.Agent.ENABLE) {
            LOGGER.warn("SkyWalking agent is disabled.");
            return;
        }


        String pluginDir = dir + "plugins";
        new PluginManager(pluginDir, inst).applyRules();

        Runtime.getRuntime().addShutdownHook(new Thread(ServiceManager.INSTANCE::shutdown, "skywalking service shutdown thread"));
    }
}
