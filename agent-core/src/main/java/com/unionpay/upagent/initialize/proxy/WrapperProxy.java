package com.unionpay.upagent.initialize.proxy;
import com.unionpay.upagent.initialize.ClassUtil;

import java.lang.reflect.Proxy;

/**
 * 代理工具类，用于创建代理对象
 */
public class WrapperProxy {

    /**
     * 为obj创建代理对象。当某方法在obj和wrapper都都是存在时，会自动执行wrapper的方法。
     * @param obj
     * @param wrapper
     * @return
     * @param <T>
     */
    public static <T> T wrap(final T obj, final T wrapper) {
        if (obj != null && wrapper != null && obj != wrapper) {
            Class<?> objClass = obj.getClass();
            T ret = (T) Proxy.newProxyInstance(objClass.getClassLoader(), ClassUtil.getAllInterfaces(objClass), new WrapperInvocationHandler(obj, wrapper));
            return ret;
        }
        return wrapper;
    }
}