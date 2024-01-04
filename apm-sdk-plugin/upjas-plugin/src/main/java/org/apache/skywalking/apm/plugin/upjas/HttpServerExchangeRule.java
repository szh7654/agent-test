//package org.apache.skywalking.apm.plugin.upjas;
//
//import net.bytebuddy.agent.builder.AgentBuilder;
//import net.bytebuddy.asm.Advice;
//import net.bytebuddy.matcher.ElementMatchers;
//import com.unionpay.upagent.initialize.AbstractRule;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.concurrent.Executor;
//
//import static net.bytebuddy.matcher.ElementMatchers.named;
//import static net.bytebuddy.matcher.ElementMatchers.takesArguments;
//
//public class HttpServerExchangeRule extends AbstractRule {
//    @Override
//    public AgentBuilder.Identified.Narrowable buildAgentChain1(AgentBuilder agentBuilder) {
//        return agentBuilder
//                .type(named("io.undertow.server.HttpServerExchange"));
//    }
//
//    @Override
//    public AgentBuilder buildAgentChain2(final AgentBuilder.Identified.Narrowable agentBuilder) {
//        return agentBuilder.transform((builder, typeDescription, classLoader, module, protectionDomain) ->
//                builder.visit(Advice.to(HttpServerExchangeInterceptor.class)
//                        .on(named("dispatch").and(takesArguments(Executor.class, Runnable.class))
//                        )
//                )
//        );
//    }
//
//    @Override
//    public String[] importedClasses() {
//        return new String[]{};
//    }
//}
