package szh;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.pool.TypePool;
import net.bytebuddy.utility.JavaModule;
//import org.apache.skywalking.apm.agent.core.boot.ServiceManager;
//import org.apache.skywalking.apm.agent.core.conf.Config;
//import org.apache.skywalking.apm.agent.core.conf.SnifferConfigInitializer;
//import org.apache.skywalking.apm.agent.core.logging.api.ILog;
//import org.apache.skywalking.apm.agent.core.logging.api.LogManager;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static net.bytebuddy.matcher.ElementMatchers.*;
import static net.bytebuddy.matcher.ElementMatchers.nameContains;

public class Main {
    //private static ILog LOGGER = LogManager.getLogger(Main.class);
    public static void initialize(File jarFile, ClassLoader classLoader, Instrumentation inst) throws IOException {
//        String jarPath = jarFile.getPath();
//        String dir = jarPath.substring(0, jarPath.lastIndexOf('/'));
//        System.out.println(dir);
//        System.setProperty("skywalking_config", dir + "/agent.config");
//        System.out.println("initialize");
//        try {
//            SnifferConfigInitializer.initializeCoreConfig("");
//        } catch (Exception e) {
//            LogManager.getLogger(Main.class).error(e, "Agent initialized failure. Shutting down.");
//            return;
//        } finally {
//            LOGGER = LogManager.getLogger(Main.class);
//        }
//        if (!Config.Agent.ENABLE) {
//            LOGGER.warn("SkyWalking agent is disabled.");
//            return;
//        }





        AgentBuilder agentBuilder = new AgentBuilder.Default()
            .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
            // when runtime attaching, only retransform up to 100 classes at once and sleep 100ms in-between as retransformation causes a stop-the-world pause
            .with(AgentBuilder.RedefinitionStrategy.BatchAllocator.ForFixedSize.ofSize(100))
            .with(AgentBuilder.RedefinitionStrategy.Listener.Pausing.of(100, TimeUnit.MILLISECONDS))
            .with(new AgentBuilder.RedefinitionStrategy.Listener.Adapter() {
                @Override
                public Iterable<? extends List<Class<?>>> onError(int index, List<Class<?>> batch, Throwable throwable, List<Class<?>> types) {
                    System.out.println("Error while redefining classes\n" + throwable.getMessage());
                    return super.onError(index, batch, throwable, types);
                }
            })
//            .with(AgentBuilder.DescriptionStrategy.Default.POOL_ONLY)
//            .with(locationStrategy)
            // ReaderMode.FAST as we don't need to read method parameter names
            .with(AgentBuilder.PoolStrategy.Default.FAST)
            .with(AgentBuilder.Listener.StreamWriting.toSystemOut().withTransformationsOnly())
            .ignore(any(), isReflectionClassLoader())
            .or(any(), classLoaderWithName("org.codehaus.groovy.runtime.callsite.CallSiteClassLoader"))
            .or(nameStartsWith("org.aspectj."))
            .or(nameStartsWith("org.groovy."))
            .or(nameStartsWith("com.p6spy."))
            .or(nameStartsWith("net.bytebuddy."))
            .or(nameStartsWith("org.stagemonitor."))
            .or(any(), classLoaderWithNamePrefix("com.newrelic."))
            .or(nameStartsWith("com.newrelic."))
            .or(any(), classLoaderWithNamePrefix("com.nr.agent."))
            .or(nameStartsWith("com.nr.agent."))
            .or(any(), classLoaderWithNamePrefix("com.dynatrace."))
            .or(nameStartsWith("com.dynatrace."))
            // AppDynamics
            .or(any(), classLoaderWithNamePrefix("com.singularity"))
            .or(nameStartsWith("com.singularity."))
            .or(any(), classLoaderWithNamePrefix("com.appdynamics."))
            .or(nameStartsWith("com.appdynamics."))
            .or(any(), classLoaderWithNamePrefix("com.instana."))
            .or(nameStartsWith("com.instana."))
            .or(any(), classLoaderWithNamePrefix("datadog."))
            .or(nameStartsWith("datadog."))
            .or(nameStartsWith("org.glowroot."))
            .or(nameStartsWith("com.compuware."))
            .or(nameStartsWith("io.sqreen."))
            .or(nameStartsWith("com.contrastsecurity."))
            .or(nameContains("javassist"))
            .or(nameContains(".asm."))
            .disableClassFormatChanges();
        agentBuilder.type(named("org.springframework.web.servlet.DispatcherServlet"))
                .transform(new AgentBuilder.Transformer.ForAdvice().include(classLoader).
                        advice(
                        named("doDispatch"), "java.lang.szh.szh.DispatcherServletInterceptor"))
            .installOn(inst);
        //LOGGER.info("123");
        //Runtime.getRuntime().addShutdownHook(new Thread(ServiceManager.INSTANCE::shutdown, "skywalking service shutdown thread"));
    }

    public static ElementMatcher.Junction<ClassLoader> classLoaderWithName(final String name) {
        return new ElementMatcher.Junction.AbstractBase<ClassLoader>() {
            @Override
            public boolean matches(ClassLoader target) {
                return target != null && target.getClass().getName().equals(name);
            }
        };
    }

    public static ElementMatcher.Junction<ClassLoader> classLoaderWithNamePrefix(final String name) {
        return new ElementMatcher.Junction.AbstractBase<ClassLoader>() {
            @Override
            public boolean matches(ClassLoader target) {
                return target != null && target.getClass().getName().startsWith(name);
            }
        };
    }

    public static ElementMatcher.Junction<ClassLoader> isReflectionClassLoader() {
        return classLoaderWithName("sun.reflect.DelegatingClassLoader")
            .or(classLoaderWithName("jdk.internal.reflect.DelegatingClassLoader"));
    }
}
