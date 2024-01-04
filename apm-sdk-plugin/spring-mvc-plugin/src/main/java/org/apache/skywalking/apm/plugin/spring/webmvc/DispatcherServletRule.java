package org.apache.skywalking.apm.plugin.spring.webmvc;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import com.unionpay.upagent.initialize.AbstractRule;

import java.util.Arrays;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class DispatcherServletRule extends AbstractRule {
    @Override
    public ElementMatcher classMatcher() {
        return named("org.springframework.web.servlet.DispatcherServlet");
    }

    @Override
    public ElementMatcher methodMatcher() {
        return named("doDispatch");
    }

    @Override
    public Class interceptorClass() {
        return DispatcherServletInterceptor.class;
    }

    @Override
    public boolean BootStrapLoaderInjection() {
        return false;
    }
}
