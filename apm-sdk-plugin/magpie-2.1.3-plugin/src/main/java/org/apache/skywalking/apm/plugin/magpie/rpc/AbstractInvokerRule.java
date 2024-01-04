package org.apache.skywalking.apm.plugin.magpie.rpc;

import com.unionpay.magpie.rpc.Invocation;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import com.unionpay.upagent.initialize.AbstractRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Magpie rpc consumer rule
 */
public class AbstractInvokerRule extends AbstractRule {
    @Override
    public AgentBuilder.Identified.Narrowable buildAgentChain1(AgentBuilder agentBuilder) {
        return agentBuilder.type(ElementMatchers
                .named("com.unionpay.magpie.rpc.protocol.AbstractInvoker"));
    }

    @Override
    public AgentBuilder buildAgentChain2(final AgentBuilder.Identified.Narrowable agentBuilder) {
        return agentBuilder.transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                builder.visit(Advice
                        .to(AbstractInvokerInterceptor.class)
                        .on(ElementMatchers.named("invoke")
                                .and(ElementMatchers.takesArgument(0, Invocation.class))
                        )));
    }

    @Override
    public String[] importedClasses() {
        return new String[]{"org.jboss.netty.channel.Channel",
                "com.unionpay.magpie.common.utils.UrlUtil",
                "com.unionpay.magpie.remoting.ChannelRegistry",
                "com.unionpay.magpie.rpc.Invocation",
                "com.unionpay.magpie.rpc.impl.RpcInvocation",
                "com.unionpay.magpie.rpc.protocol.AbstractInvoker"};
    }
}
