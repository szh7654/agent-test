package com.unionpay.upagent.initialize;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;

import javax.sound.midi.Instrument;
import java.io.*;
import java.lang.instrument.Instrumentation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static net.bytebuddy.matcher.ElementMatchers.*;
import static net.bytebuddy.matcher.ElementMatchers.nameContains;

public class PluginManager {
    private static ILog LOGGER = LogManager.getLogger(PluginManager.class);
    Instrumentation inst;
    List<URLClassLoader> pluginClassLoaders = new ArrayList<>();
    List<AbstractRule> rules = new ArrayList<>();

    public PluginManager(String pluginDir, Instrumentation inst) {
        this.inst = inst;
        File[] pluginFiles = new File(pluginDir).listFiles();
        for (File pluginFile : pluginFiles) {
            if (pluginFile.isFile() && pluginFile.getName().endsWith(".jar")) {
                URLClassLoader classLoader = null;
                try {
                    classLoader = new URLClassLoader(new URL[]{pluginFile.toURI().toURL()}, null);
                } catch (MalformedURLException e) {
                    LOGGER.error(e, "Failed to load plugin file: {}", pluginFile.getAbsolutePath());
                }
                pluginClassLoaders.add(classLoader);
            }
        }
    }

    public void applyRules() {
        AgentBuilder agentBuilder = ByteBuddyUtil.getAgentBuilder();
        for (URLClassLoader classLoader : pluginClassLoaders) {
            Enumeration<URL> urls = null;
            try {
                urls = classLoader.getResources("skywalking-plugin.def");
            } catch (IOException e) {
                LOGGER.warn("Load skywalking-plugin.def error in {}", classLoader.getURLs());
            }
            if (!urls.hasMoreElements()) {
                LOGGER.warn("No skywalking-plugin.def found in {}", classLoader.getURLs());
                continue;
            }
            URL defUrl = urls.nextElement();
            LOGGER.info("Load skywalking-plugin.def success: {}", defUrl);
            try (InputStream inputStream = defUrl.openStream()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String ruleClassName;
                while ((ruleClassName = reader.readLine()) != null) {
                    try {
                        AbstractRule rule = (AbstractRule) Class.forName(ruleClassName, true, classLoader).newInstance();
                        LOGGER.info("Load rule class {} success", rule.getClass().getCanonicalName());
                        agentBuilder = agentBuilder.type(rule.classMatcher())
                                .transform(new AgentBuilder.Transformer.ForAdvice()
                                        .include(classLoader)
                                        .advice(rule.methodMatcher(), rule.interceptorClass().getCanonicalName()));

                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                        LOGGER.error(e, "Failed to load rule class: {}", defUrl);
                    }
                }
            } catch (IOException e) {
                LOGGER.error("failed to read .def file", e);
            }
        }
        agentBuilder.installOn(inst);
        LOGGER.info("Install transform success");
    }
}
