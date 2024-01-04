package szh;


import com.unionpay.upagent.initialize.Initializer;
import net.bytebuddy.asm.Advice;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DispatcherServletInterceptor {

    @Advice.OnMethodEnter(inline = true)
    public static String enter(@Advice.Argument(0) HttpServletRequest request,
                                     @Advice.Argument(1) HttpServletResponse response) {
        System.out.println(response.getStatus());
        System.out.println(request.getRequestURL());
        new MyClass(request, response).echo();
        return "12345";
    }

    @Advice.OnMethodExit(inline = true, onThrowable = Throwable.class)
    public static void exit(@Advice.Enter String obj,
                            @Advice.Argument(1) HttpServletResponse response,
                            @Advice.Thrown(readOnly = true) Throwable t){
        System.out.println(obj);
    }
}
