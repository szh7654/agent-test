//package org.apache.skywalking.apm.plugin.interceptors;
//
//import net.bytebuddy.asm.Advice;
//import org.apache.catalina.connector.Request;
//import org.apache.skywalking.apm.agent.core.advice.AbstractMethodInterceptor;
//import org.apache.skywalking.apm.agent.core.context.CarrierItem;
//import org.apache.skywalking.apm.agent.core.context.ContextCarrier;
//import org.apache.skywalking.apm.agent.core.context.ContextManager;
//import org.apache.skywalking.apm.agent.core.context.tag.Tags;
//import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
//import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
//import org.apache.skywalking.apm.agent.core.logging.api.ILog;
//import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
//import org.apache.skywalking.apm.agent.core.util.CollectionUtil;
//import org.apache.skywalking.apm.agent.core.util.MethodUtil;
//import org.apache.skywalking.apm.network.trace.component.ComponentsDefine;
//import org.apache.skywalking.apm.util.StringUtil;
//import org.apache.tomcat.util.http.Parameters;
//
//import javax.servlet.http.HttpServletResponse;
//import java.util.Enumeration;
//import java.util.HashMap;
//import java.util.Map;
//
//public class  TomcatInvokeInterceptor extends AbstractMethodInterceptor {
//    public static final ILog LOGGER = LogManager.getLogger(TomcatInvokeInterceptor.class);
//    public static final String className = "org.apache.catalina.core.StandardHostValve";
//    public static final String method = "invoke";
//    public static final int HTTP_PARAMS_LENGTH_THRESHOLD = 1024;
//    public static final String FORWARD_REQUEST_FLAG = "SW_FORWARD_REQUEST_FLAG";
//    private static final String SERVLET_RESPONSE_CLASS = "javax.servlet.http.HttpServletResponse";
//    private static final String GET_STATUS_METHOD = "getStatus";
//    public static boolean IS_SERVLET_GET_STATUS_METHOD_EXIST;
//    public static boolean IS_SERVLET_GET_STATUS_METHOD_EXIST_FLAG = false;
//
//    @Advice.OnMethodEnter(inline = true)
//    public static void enter(@Advice.Argument(0) Request request) {
//        if (!IS_SERVLET_GET_STATUS_METHOD_EXIST_FLAG) {
//            IS_SERVLET_GET_STATUS_METHOD_EXIST = MethodUtil.isMethodExist(TomcatInvokeInterceptor.class.getClassLoader(), SERVLET_RESPONSE_CLASS, GET_STATUS_METHOD);
//            IS_SERVLET_GET_STATUS_METHOD_EXIST_FLAG = true;
//        }
//        try {
//            LOGGER.info("enter method, request====" + request.toString());
//            ContextCarrier contextCarrier = new ContextCarrier();
//            LOGGER.info("ContextCarrier: " + contextCarrier);
//            CarrierItem next = contextCarrier.items();
//            while (next.hasNext()) {
//                LOGGER.info("CarrierItem: " + next);
//                next = next.next();
//                next.setHeadValue(request.getHeader(next.getHeadKey()));
//            }
//            String operationName = String.join(":", request.getMethod(), request.getRequestURI());
//            LOGGER.info("operationName: " + operationName);
//            AbstractSpan span = ContextManager.createEntrySpan(operationName, contextCarrier);
//            Tags.URL.set(span, request.getRequestURL().toString());
//            Tags.HTTP.METHOD.set(span, request.getMethod());
//            span.setComponent(ComponentsDefine.TOMCAT);
//            SpanLayer.asHttp(span);
//            LOGGER.info("span: " + span);
//            LOGGER.info("Tags: " + Tags.URL + "====" + Tags.HTTP.METHOD);
//        } catch (Throwable t) {
//            AbstractSpan span = ContextManager.activeSpan();
//            span.log(t);
//        }
//    }
//
//    @Advice.OnMethodExit(inline = true)
//    public static void exit(@Advice.Argument(0) Request request, @Advice.Argument(1) HttpServletResponse response) {
//        try {
//            LOGGER.info("exit method, args====" + request.toString());
//            AbstractSpan span = ContextManager.activeSpan();
//            LOGGER.info("span: " + span);
//            if (IS_SERVLET_GET_STATUS_METHOD_EXIST) {
//                Tags.HTTP_RESPONSE_STATUS_CODE.set(span, response.getStatus());
//                if (response.getStatus() >= 400) {
//                    span.errorOccurred();
//                }
//            }
//            // Active HTTP parameter collection automatically in the profiling context.
//            if (span.isProfiling()) {
//                collectHttpParam(request, span);
//            }
//            ContextManager.getRuntimeContext().remove(FORWARD_REQUEST_FLAG);
//            ContextManager.stopSpan();
//        } catch (Throwable t) {
//            AbstractSpan span = ContextManager.activeSpan();
//            span.log(t);
//        }
//    }
//
//    static public void collectHttpParam(Request request, AbstractSpan span) {
//        final Map<String, String[]> parameterMap = new HashMap<>();
//        final org.apache.coyote.Request coyoteRequest = request.getCoyoteRequest();
//        final Parameters parameters = coyoteRequest.getParameters();
//        for (final Enumeration<String> names = parameters.getParameterNames(); names.hasMoreElements(); ) {
//            final String name = names.nextElement();
//            parameterMap.put(name, parameters.getParameterValues(name));
//        }
//
//        if (!parameterMap.isEmpty()) {
//            String tagValue = CollectionUtil.toString(parameterMap);
//            tagValue = StringUtil.cut(tagValue, HTTP_PARAMS_LENGTH_THRESHOLD);
//            Tags.HTTP.PARAMS.set(span, tagValue);
//        }
//    }
//
//
//    @Override
//    public String getClassName() {
//        return className;
//    }
//
//    @Override
//    public String getMethod() {
//        return method;
//    }
//}
