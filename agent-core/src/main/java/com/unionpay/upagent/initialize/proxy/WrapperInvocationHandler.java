package com.unionpay.upagent.initialize.proxy;

import com.unionpay.upagent.initialize.ClassUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class WrapperInvocationHandler<T> implements InvocationHandler {
    T obj;
    T wrapper;

    public WrapperInvocationHandler(T obj, T wrapper) {
        this.obj = obj;
        this.wrapper = wrapper;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?> wrapperClass = wrapper.getClass();
        try {
            try {
                if (method.getDeclaringClass().isAssignableFrom(wrapperClass))
                    return method.invoke(wrapper, args);

                final Method specific = ClassUtil.getDeclaredMethodDeep(wrapperClass, method.getName(), method.getParameterTypes());
                if (specific != null)
                    return specific.invoke(wrapper, args);
            }
            catch (final IllegalArgumentException e) {
            }

            return method.invoke(obj, args);
        }
        catch (final InvocationTargetException e) {
            throw e.getCause();
        }
        catch (final IllegalAccessException e) {
            final IllegalAccessError error = new IllegalAccessError(e.getMessage());
            error.setStackTrace(e.getStackTrace());
            throw error;
        }
    }

    public T getObj() {
        return obj;
    }

    public T getWrapper() {
        return wrapper;
    }
}
