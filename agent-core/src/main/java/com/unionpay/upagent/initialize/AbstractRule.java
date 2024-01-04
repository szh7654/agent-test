package com.unionpay.upagent.initialize;


import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.List;

public abstract class AbstractRule {
    public abstract AgentBuilder.Identified.Narrowable buildAgentChain1(final AgentBuilder agentBuilder);

    public abstract AgentBuilder buildAgentChain2(final AgentBuilder.Identified.Narrowable agentBuilder);

    public abstract String[] importedClasses();
}
