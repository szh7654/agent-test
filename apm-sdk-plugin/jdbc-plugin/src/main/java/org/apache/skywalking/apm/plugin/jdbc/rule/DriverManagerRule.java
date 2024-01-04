package org.apache.skywalking.apm.plugin.jdbc.rule;

import com.unionpay.upagent.initialize.AbstractRule;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.Arrays;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

public class DriverManagerRule extends AbstractRule {
    @Override
    public ElementMatcher classMatcher() {
        return named("java.sql.DriverManager");
    }

    @Override
    public ElementMatcher methodMatcher() {
        return isPrivate()
                .and(isStatic())
                .and(named("isDriverAllowed").and(takesArgument(1, Class.class)));
    }

    @Override
    public Class interceptorClass() {
        return DriverMangerInterceptor.class;
    }

    @Override
    public boolean BootStrapLoaderInjection() {
        return true;
    }
}
