package org.apache.skywalking.apm.plugin.upjas;

import net.bytebuddy.asm.Advice;
import org.apache.skywalking.apm.agent.core.context.ContextManager;

public class HttpServerExchangeInterceptor {

    @Advice.OnMethodEnter
    public static void enter(@Advice.Argument(value = 1, readOnly = false) Runnable runnable) {
        if (ContextManager.isActive()) {
            if (runnable != null) {
                runnable = new SWRunnable(runnable, ContextManager.capture());
            }
        }
    }
}
