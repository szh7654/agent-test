package org.apache.skywalking.apm.plugin.magpie.rpc;

import com.unionpay.magpie.common.utils.UrlUtil;
import com.unionpay.magpie.remoting.ChannelRegistry;
import com.unionpay.magpie.rpc.Invocation;
import com.unionpay.magpie.rpc.impl.RpcInvocation;
import com.unionpay.magpie.rpc.protocol.AbstractInvoker;
import net.bytebuddy.asm.Advice;
import org.apache.skywalking.apm.agent.core.context.CarrierItem;
import org.apache.skywalking.apm.agent.core.context.ContextCarrier;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.network.trace.component.OfficialComponent;
import org.jboss.netty.channel.Channel;

import java.net.InetSocketAddress;

/**
 * a
 * Magpie rpc consumer interceptor
 */
public class AbstractInvokerInterceptor {
    @Advice.OnMethodEnter(inline = true)
    public static AbstractSpan enter(@Advice.This AbstractInvoker self,
                                     @Advice.Argument(0) Invocation invocation) {
        Channel channel = ChannelRegistry.getChannel(self.getUrl().getHostAndPort());
        String consumerAddress = UrlUtil.getAddressKey((InetSocketAddress) channel.getLocalAddress());
        String providerAddress = UrlUtil.getAddressKey((InetSocketAddress) channel.getRemoteAddress());
        ((RpcInvocation)invocation).setAttachment("_MAGPIE_CONSUMER_ADDRESS", consumerAddress);
        ((RpcInvocation)invocation).setAttachment("_MAGPIE_PROVIDER_ADDRESS", providerAddress);
        String url = self.getUrl().getPath();
        String path = self.getUrl().getPath();
        String serviceClassName = path.substring(path.lastIndexOf(".") + 1);
        String method = serviceClassName + "." + invocation.getMethodName();
        final ContextCarrier contextCarrier = new ContextCarrier();
        AbstractSpan exitSpan = ContextManager.createExitSpan("Magpie[Client]: " + method, contextCarrier, providerAddress);
        CarrierItem next = contextCarrier.items();
        while (next.hasNext()) {
            next = next.next();
            ((RpcInvocation)invocation).setAttachment(next.getHeadKey(), next.getHeadValue());
        }
        exitSpan.tag("Consumer Address", consumerAddress);
        exitSpan.tag("Provider Address", providerAddress);
        exitSpan.tag("RPC Method", path + "." + invocation.getMethodName());
        exitSpan.setComponent(new OfficialComponent(150, "Magpie"));
        SpanLayer.asRPCFramework(exitSpan);
        return exitSpan;
    }

    @Advice.OnMethodExit(inline = true, onThrowable = Exception.class)
    public static void exit(@Advice.Thrown(readOnly = true) Exception exception,
                            @Advice.Enter AbstractSpan exitSpan) {
        if (exception != null) {
            exitSpan.log(exception);
        }
        ContextManager.stopSpan(exitSpan);
    }
}
