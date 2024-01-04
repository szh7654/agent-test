package org.apache.skywalking.apm.plugin.upjas;

import com.unionpay.upagent.initialize.AbstractRule;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;


import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

public class ConnectorsRule extends AbstractRule {

    @Override
    public AgentBuilder.Identified.Narrowable buildAgentChain1(AgentBuilder agentBuilder) {
        return agentBuilder
                .type(named("io.undertow.server.Connectors"));
    }

    @Override
    public AgentBuilder buildAgentChain2(final AgentBuilder.Identified.Narrowable agentBuilder) {
        return agentBuilder.transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                builder.visit(Advice.to(ConnectorsInterceptor.class)
                        .on(named("executeRootHandler").and(takesArguments(HttpHandler.class, HttpServerExchange.class))))
        );
    }

    @Override
    public String[] importedClasses() {
        return new String[]{"io.undertow.server.ExchangeCompletionListener",
                "io.undertow.server.HttpHandler",
                "io.undertow.server.HttpServerExchange",
                "io.undertow.util.HeaderMap"};
    }
}