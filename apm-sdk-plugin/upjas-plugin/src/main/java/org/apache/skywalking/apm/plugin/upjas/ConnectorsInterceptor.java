package org.apache.skywalking.apm.plugin.upjas;

import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import net.bytebuddy.asm.Advice;
import org.apache.skywalking.apm.agent.core.context.CarrierItem;
import org.apache.skywalking.apm.agent.core.context.ContextCarrier;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.tag.Tags;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine;

public class ConnectorsInterceptor {
    public static final ILog LOGGER = LogManager.getLogger(ConnectorsInterceptor.class);

    @Advice.OnMethodEnter
    public static AbstractSpan enter(@Advice.Argument(0) HttpHandler handler,
                                     @Advice.Argument(1) HttpServerExchange exchange) {
        final HeaderMap headers = exchange.getRequestHeaders();
        final ContextCarrier carrier = new ContextCarrier();
        CarrierItem items = carrier.items();
        while (items.hasNext()) {
            items = items.next();
            items.setHeadValue(headers.getFirst(items.getHeadKey()));
        }
        String operationName = exchange.getRequestPath();

        final AbstractSpan span = ContextManager.createEntrySpan(exchange.getRequestMethod() + ":" + operationName, carrier);
        Tags.URL.set(span, exchange.getRequestURL());
        Tags.HTTP.METHOD.set(span, exchange.getRequestMethod().toString());
        span.setComponent(ComponentsDefine.UNDERTOW);
        SpanLayer.asHttp(span);
        try {
            span.prepareForAsync();
            exchange.addExchangeCompleteListener(new ExchangeCompletionListener() {
                @Override
                public void exchangeEvent(HttpServerExchange httpServerExchange, NextListener nextListener) {
                    nextListener.proceed();
                    Tags.HTTP_RESPONSE_STATUS_CODE.set(span, httpServerExchange.getStatusCode());
                    if (httpServerExchange.getStatusCode() >= 400) {
                        span.errorOccurred();
                    }
                    span.asyncFinish();
                }
            });
        } catch (Throwable e) {
            ContextManager.activeSpan().log(e);
        }
        return span;
    }

    @Advice.OnMethodExit(onThrowable = Exception.class)
    public static void exit(@Advice.Thrown(readOnly = true) Exception exception,
                            @Advice.Enter AbstractSpan span) {
        if (exception != null) {
            span.log(exception);
        }
        ContextManager.stopSpan(span);
    }
}
