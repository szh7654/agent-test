package com.unionpay.upagent.initialize;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static net.bytebuddy.matcher.ElementMatchers.*;
import static net.bytebuddy.matcher.ElementMatchers.nameContains;

public class ByteBuddyUtil {
    public static AgentBuilder getAgentBuilder() {
        AgentBuilder agentBuilder = new AgentBuilder.Default()
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                // when runtime attaching, only retransform up to 100 classes at once and sleep 100ms in-between as retransformation causes a stop-the-world pause
                .with(AgentBuilder.RedefinitionStrategy.BatchAllocator.ForFixedSize.ofSize(100))
                .with(AgentBuilder.RedefinitionStrategy.Listener.Pausing.of(100, TimeUnit.MILLISECONDS))
//                .with(new AgentBuilder.RedefinitionStrategy.Listener.Adapter() {
//                    @Override
//                    public Iterable<? extends List<Class<?>>> onError(int index, List<Class<?>> batch, Throwable throwable, List<Class<?>> types) {
//                        System.out.println("Error while redefining classes\n" + throwable.getMessage());
//                        return super.onError(index, batch, throwable, types);
//                    }
//                })
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
        return agentBuilder;
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
