package org.apache.skywalking.apm.plugin.magpie.binary;

import com.unionpay.magpie.remoting.Request;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import com.unionpay.upagent.initialize.AbstractRule;

import net.bytebuddy.matcher.ElementMatchers;

import java.util.Arrays;
import java.util.List;

/**
 * Magpie binary consumer rule
 */
public class OneToOneInvokerWrapperCallRule extends AbstractRule {

    @Override
    public AgentBuilder.Identified.Narrowable buildAgentChain1(AgentBuilder agentBuilder) {
        return agentBuilder.type(ElementMatchers
                .named("com.unionpay.magpie.client.impl.OneToOneInvokerWrapper"));
    }

    @Override
    public AgentBuilder buildAgentChain2(final AgentBuilder.Identified.Narrowable agentBuilder) {
        return agentBuilder.transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                builder.visit(Advice
                        .to(OneToOneInvokerWrapperCallInterceptor.class)
                        .on(ElementMatchers.named("call")
                                .and(ElementMatchers.takesArgument(0, Request.class))
                        )));
    }

    @Override
    public String[] importedClasses() {
        return new String[]{"com.unionpay.magpie.client.impl.OneToOneInvokerWrapper",
                "com.unionpay.magpie.remoting.Request",
                "com.unionpay.magpie.remoting.support.magpie.MagpieRequest"};
    }
}
