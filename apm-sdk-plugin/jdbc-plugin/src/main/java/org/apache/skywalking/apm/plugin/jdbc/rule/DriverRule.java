package org.apache.skywalking.apm.plugin.jdbc.rule;

import com.unionpay.upagent.initialize.AbstractRule;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
//import com.unionpay.upagent.initialize.AbstractRule;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class DriverRule extends AbstractRule {


    @Override
    public ElementMatcher classMatcher() {
        return hasSuperType(named("java.sql.Driver"))
                .and(not(named("io.opentracing.contrib.jdbc.TracingDriver")));
    }

    @Override
    public ElementMatcher methodMatcher() {
        return not(isAbstract())
                .and(named("connect")
                        .and(takesArguments(String.class, Properties.class))
                );
    }

    @Override
    public Class interceptorClass() {
        return DriverInterceptor.class;
    }

    @Override
    public boolean BootStrapLoaderInjection() {
        return true;
    }
}
