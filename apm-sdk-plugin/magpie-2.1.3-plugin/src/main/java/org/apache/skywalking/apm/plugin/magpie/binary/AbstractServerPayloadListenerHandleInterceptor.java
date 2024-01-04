package org.apache.skywalking.apm.plugin.magpie.binary;

import com.unionpay.magpie.remoting.support.magpie.MagpieRequest;
import net.bytebuddy.asm.Advice;
import org.apache.skywalking.apm.agent.core.context.CarrierItem;
import org.apache.skywalking.apm.agent.core.context.ContextCarrier;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.network.trace.component.OfficialComponent;

/**
 * Magpie binary provider interceptor
 */
public class AbstractServerPayloadListenerHandleInterceptor {
    @Advice.OnMethodEnter(inline = true)
    public static AbstractSpan enter(@Advice.Argument(0) MagpieRequest request) {
        String serviceId = request.getServiceId();
        ContextCarrier contextCarrier = new ContextCarrier();
        CarrierItem next = contextCarrier.items();
        while (next.hasNext()) {
            next = next.next();
            next.setHeadValue((String) request.getAttachments().get(next.getHeadKey()));
        }
        AbstractSpan span = ContextManager.createEntrySpan("Magpie[server]: " + serviceId, contextCarrier);
        span.setOperationName("Magpie[server]: " + serviceId);
        SpanLayer.asRPCFramework(span);
        span.setComponent(new OfficialComponent(150, "Magpie"));
        return span;
    }

    @Advice.OnMethodExit(inline = true, onThrowable = Exception.class)
    public static void exit(@Advice.Thrown(readOnly = true) Exception exception, @Advice.Enter AbstractSpan span) {
        if (exception != null) {
            span.log(exception);
        }
        ContextManager.stopSpan(span);
    }

}
