package org.apache.skywalking.apm.plugin.spring.webmvc;

import com.unionpay.upagent.initialize.Initializer;
import net.bytebuddy.asm.Advice;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.tag.Tags;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DispatcherServletInterceptor {
    private static ILog LOGGER = LogManager.getLogger(DispatcherServletInterceptor.class);
    @Advice.OnMethodEnter(inline = true)
    public static AbstractSpan enter(@Advice.Argument(0) HttpServletRequest request,
                                     @Advice.Argument(1) HttpServletResponse response) {
        System.out.println("DispatcherServletInterceptor enter");
        System.out.println(request);
        LOGGER.info(request.toString());
        LOGGER.info(response.toString());
        AbstractSpan localSpan = ContextManager.createLocalSpan("DispatcherServlet.doDispatch");
        localSpan.setComponent(ComponentsDefine.SPRING_MVC_ANNOTATION);
        String url = request.getMethod() + " " + request.getRequestURL();
        localSpan.tag(Tags.URL, url);
        SpanLayer.asHttp(localSpan);
        return localSpan;
    }

    @Advice.OnMethodExit(inline = true, onThrowable = Throwable.class)
    public static void exit(@Advice.Enter AbstractSpan localSpan,
                            @Advice.Argument(1) HttpServletResponse response,
                            @Advice.Thrown(readOnly = true) Throwable t){
        Tags.HTTP_RESPONSE_STATUS_CODE.set(localSpan, response.getStatus());
        if (response.getStatus() >= 400) {
            localSpan.errorOccurred();
        }

        if (t != null) {
            ContextManager.activeSpan().log(t);
        }
        ContextManager.stopSpan(localSpan);
    }
}
