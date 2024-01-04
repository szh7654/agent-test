package org.apache.skywalking.apm.plugin.jdbc.rule;

import com.unionpay.upagent.initialize.AbstractRule;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.Arrays;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

public class DriverManagerRule extends AbstractRule {
    @Override
    public AgentBuilder.Identified.Narrowable buildAgentChain1(AgentBuilder agentBuilder) {
        return agentBuilder.type(named("java.sql.DriverManager"));
    }

    @Override
    public AgentBuilder buildAgentChain2(final AgentBuilder.Identified.Narrowable agentBuilder) {
        return agentBuilder.transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                builder.visit(Advice
                        .to(DriverMangerInterceptor.class)
                        .on(isPrivate()
                                .and(isStatic())
                                .and(named("isDriverAllowed").and(takesArgument(1, Class.class)))

                        )
                )
        );
    }
    @Override
    public String[] importedClasses() {
        return new String[]{};
    }
}
