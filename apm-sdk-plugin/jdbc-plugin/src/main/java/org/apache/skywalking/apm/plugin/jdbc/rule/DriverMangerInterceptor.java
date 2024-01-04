package org.apache.skywalking.apm.plugin.jdbc.rule;

import net.bytebuddy.asm.Advice;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.plugin.jdbc.TracingDriver;

public class DriverMangerInterceptor {
    private static final ILog LOGGER = LogManager.getLogger(DriverMangerInterceptor.class);

    private static final String DRIVER_MANAGER_INTERCEPTOR = "org.apache.skywalking.apm.plugin.jdbc.rule.DriverMangerInterceptor";

    private static final String DRIVER_INTERCEPTOR = "org.apache.skywalking.apm.plugin.jdbc.rule.DriverInterceptor";

    private static final String TRACING_DRIVER = "org.apache.skywalking.apm.plugin.jdbc.TracingDriver";

    @Advice.OnMethodEnter(inline = true)
    public static boolean enter(@Advice.Argument(1) Class<?> caller) {
        if (DRIVER_MANAGER_INTERCEPTOR.equals(caller.getName()) ||
                DRIVER_INTERCEPTOR.equals(caller.getName()) ||
                TRACING_DRIVER.equals(caller.getName()))
            return true;
        return false;
    }


    @Advice.OnMethodExit(inline = true)
    public static void exit(@Advice.Argument(1) Class<?> caller,
                            @Advice.Enter boolean result,
                            @Advice.Return(readOnly = false) boolean returned) {
        if (result) {
            returned = true;
        }
    }
}
