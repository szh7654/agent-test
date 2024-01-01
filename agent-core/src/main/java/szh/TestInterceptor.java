package szh;

import net.bytebuddy.asm.Advice;

public class TestInterceptor {

    @Advice.OnMethodEnter
    public static void enter() {
        MyClass myClass = new MyClass();
        myClass.echo();
    }
}
