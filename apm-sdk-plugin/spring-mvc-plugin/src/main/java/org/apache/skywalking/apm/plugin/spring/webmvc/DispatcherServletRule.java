package org.apache.skywalking.apm.plugin.spring.webmvc;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import com.unionpay.upagent.initialize.AbstractRule;

import java.util.Arrays;
import java.util.List;

public class DispatcherServletRule extends AbstractRule {
    @Override
    public AgentBuilder.Identified.Narrowable buildAgentChain1(AgentBuilder agentBuilder) {
        return agentBuilder.type(ElementMatchers
                .named("org.springframework.web.servlet.DispatcherServlet"));
    }

    @Override
    public AgentBuilder buildAgentChain2(final AgentBuilder.Identified.Narrowable agentBuilder) {
        return agentBuilder.transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                builder.visit(Advice
                        .to(DispatcherServletInterceptor.class)
                        .on(ElementMatchers.named("doDispatch"))));
    }

    @Override
    public String[] importedClasses() {
        return new String[]{"javax.servlet.http.HttpServletRequest",
                "javax.servlet.http.HttpServletResponse"};
    }
}
