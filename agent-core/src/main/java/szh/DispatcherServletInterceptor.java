package szh;


import net.bytebuddy.asm.Advice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DispatcherServletInterceptor {
    @Advice.OnMethodEnter(inline = true)
    public static MyClass enter(@Advice.Argument(0) HttpServletRequest request,
                                     @Advice.Argument(1) HttpServletResponse response) {
        MyClass  obj = new MyClass(request, response);
        return obj;
    }

    @Advice.OnMethodExit(inline = true, onThrowable = Throwable.class)
    public static void exit(@Advice.Enter MyClass obj,
                            @Advice.Argument(1) HttpServletResponse response,
                            @Advice.Thrown(readOnly = true) Throwable t){
        obj.act();
    }
}
