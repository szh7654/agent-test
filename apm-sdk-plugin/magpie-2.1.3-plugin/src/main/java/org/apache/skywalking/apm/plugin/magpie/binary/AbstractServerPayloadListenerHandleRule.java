package org.apache.skywalking.apm.plugin.magpie.binary;

import com.unionpay.magpie.remoting.support.magpie.MagpieRequest;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import com.unionpay.upagent.initialize.AbstractRule;

import java.util.Arrays;
import java.util.List;

/**
 * Magpie binary provider
 */
public class AbstractServerPayloadListenerHandleRule extends AbstractRule {
    @Override
    public AgentBuilder.Identified.Narrowable buildAgentChain1(AgentBuilder agentBuilder) {
        return agentBuilder.type(ElementMatchers
                .named("com.unionpay.magpie.server.AbstractServerPayloadListener"));
    }

    @Override
    public AgentBuilder buildAgentChain2(final AgentBuilder.Identified.Narrowable agentBuilder) {
        return agentBuilder.transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                builder.visit(Advice
                        .to(AbstractServerPayloadListenerHandleInterceptor.class)
                        .on(ElementMatchers.named("handle")
                                .and(ElementMatchers.takesArgument(0, MagpieRequest.class))
                        )));
    }
    @Override
    public String[] importedClasses() {
        return new String[]{"com.unionpay.magpie.remoting.support.magpie.MagpieRequest"};
    }
}
