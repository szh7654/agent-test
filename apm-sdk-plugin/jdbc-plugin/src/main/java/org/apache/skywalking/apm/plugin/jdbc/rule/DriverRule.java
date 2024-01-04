package org.apache.skywalking.apm.plugin.jdbc.rule;

import com.unionpay.upagent.initialize.AbstractRule;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
//import com.unionpay.upagent.initialize.AbstractRule;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class DriverRule extends AbstractRule {
    @Override
    public AgentBuilder.Identified.Narrowable buildAgentChain1(AgentBuilder agentBuilder) {
        return agentBuilder.type(
                hasSuperType(named("java.sql.Driver"))
                        .and(not(named("io.opentracing.contrib.jdbc.TracingDriver"))));
    }

    @Override
    public AgentBuilder buildAgentChain2(final AgentBuilder.Identified.Narrowable agentBuilder) {
        return agentBuilder.transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                builder.visit(Advice
                        .to(DriverInterceptor.class)
                        .on(not(isAbstract())
                                .and(named("connect")
                                        .and(takesArguments(String.class, Properties.class))
                                )
                        )
                )
        );
    }

    @Override
    public String[] importedClasses() {
        return new String[]{};
    }
}
