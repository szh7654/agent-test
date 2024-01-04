package com.unionpay.upagent.initialize;


import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.List;

public abstract class AbstractRule {

    public abstract ElementMatcher classMatcher();

    public abstract ElementMatcher methodMatcher();

    public abstract Class interceptorClass();

    public abstract boolean BootStrapLoaderInjection();
}
