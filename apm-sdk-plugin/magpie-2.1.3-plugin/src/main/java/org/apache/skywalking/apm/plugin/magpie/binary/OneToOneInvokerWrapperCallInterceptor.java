package org.apache.skywalking.apm.plugin.magpie.binary;

import com.unionpay.magpie.client.impl.OneToOneInvokerWrapper;
import com.unionpay.magpie.remoting.Request;
import com.unionpay.magpie.remoting.support.magpie.MagpieRequest;
import net.bytebuddy.asm.Advice;
import org.apache.skywalking.apm.agent.core.context.CarrierItem;
import org.apache.skywalking.apm.agent.core.context.ContextCarrier;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.network.trace.component.OfficialComponent;

import java.util.Map;

/**
 * Magpie binary consumer interceptor
 */
public class OneToOneInvokerWrapperCallInterceptor {
    private static final ILog LOGGER = LogManager.getLogger(OneToOneInvokerWrapperCallInterceptor.class);

    @Advice.OnMethodEnter(inline = true)
    public static AbstractSpan enter(@Advice.Argument(0) Request request, @Advice.This OneToOneInvokerWrapper invoker) {
        MagpieRequest magpieRequest = (MagpieRequest) request;
        String serviceId = invoker.getServiceId();
        if (serviceId == null) {
            serviceId = request.getServiceId();
        }
        String remoteAddress = invoker.getUrl().getHostAndPort();

        final ContextCarrier contextCarrier = new ContextCarrier();
        AbstractSpan exitSpan = ContextManager.createExitSpan("Magpie[client]: " + serviceId, contextCarrier, remoteAddress);
        CarrierItem next = contextCarrier.items();

        while (next.hasNext()) {
            next = next.next();
            Map<String, Object> attachments = magpieRequest.getAttachments();
            attachments.put(next.getHeadKey(), next.getHeadValue());
            magpieRequest.setAttachments(attachments);
        }
        exitSpan.tag("ServiceId", serviceId);
        exitSpan.tag("Provider Address", invoker.getUrl().getHostAndPort());
        exitSpan.tag("Type", "Binary");
        exitSpan.setComponent(new OfficialComponent(150, "Magpie"));
        exitSpan.setOperationName("Magpie Consumer: " + serviceId);
        SpanLayer.asRPCFramework(exitSpan);
        return exitSpan;
    }

    @Advice.OnMethodExit(inline = true, onThrowable = Exception.class)
    public static void exit(@Advice.Thrown(readOnly = true) Exception exception, @Advice.Enter AbstractSpan span) {
        if (exception != null) {
            span.log(exception);
        }
        ContextManager.stopSpan(span);
    }
}
