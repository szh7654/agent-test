package org.apache.skywalking.apm.plugin.magpie.rpc;

import com.unionpay.magpie.rpc.Invocation;
import net.bytebuddy.asm.Advice;
import org.apache.skywalking.apm.agent.core.context.CarrierItem;
import org.apache.skywalking.apm.agent.core.context.ContextCarrier;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.network.trace.component.OfficialComponent;

import java.util.Map;

/**
 * Magpie rpc provider interceptor
 */
public class AbstractProxyInvokerInterceptor {
    @Advice.OnMethodEnter(inline = true)
    public static AbstractSpan enter(@Advice.Argument(0) Invocation invocation) {
        ContextCarrier contextCarrier = new ContextCarrier();
        CarrierItem next = contextCarrier.items();
        Map<String, Object> attachments = invocation.getAttachments();
        while (next.hasNext()) {
            next = next.next();
            next.setHeadValue((String) attachments.get(next.getHeadKey()));
        }

        Object serviceId = attachments.get("serviceId");
        Object magpieConsumerAddress = attachments.get("_MAGPIE_CONSUMER_ADDRESS");
        Object serviceVersion = attachments.get("serviceVersion");
        String path = attachments.get("path") == null ? "" : attachments.get("path").toString();

        String serviceClassName = path.substring(path.lastIndexOf(".") + 1);
        String method = serviceClassName + "." + invocation.getMethodName();
        String magpiePath = "Magpie[Server]: " + method;
        AbstractSpan span = ContextManager.createEntrySpan(magpiePath, contextCarrier);


        span.tag("ServiceId", serviceId == null ? "" : serviceId.toString());
        span.tag("Consumer Address", magpieConsumerAddress == null ? "" : magpieConsumerAddress.toString());
        span.tag("ServiceVersion", serviceVersion == null ? "" : serviceVersion.toString());
        span.tag("Type", "RPC");
        span.tag("RPC Method", path + "." + invocation.getMethodName());
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
