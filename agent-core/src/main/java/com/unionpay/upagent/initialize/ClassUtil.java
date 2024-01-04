package com.unionpay.upagent.initialize;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;

public class ClassUtil {
    /**
     * 将当前接口/类实现的接口添加至set中
     * @param iface 当前接口/类
     * @param set
     */
    private static void recurse(final Class<?> iface, final HashSet<Class<?>> set) {
        if (set.contains(iface))
            return;

        set.add(iface);
        for (final Class<?> extended : iface.getInterfaces())
            recurse(extended, set);
    }

    /**
     * 返回该class实现的所有接口
     * @param cls 目标类
     * @return 该class实现的所有接口
     */
    public static Class<?>[] getAllInterfaces(final Class<?> cls) {
        Class<?> parent = cls;
        Class<?>[] ifaces = null;
        HashSet<Class<?>> set = null;
        do {
            ifaces = parent.getInterfaces();
            if (ifaces.length == 0)
                continue;

            if (set == null)
                set = new HashSet<>(4);

            for (final Class<?> iface : ifaces)
                recurse(iface, set);
        }
        while ((parent = parent.getSuperclass()) != null);
        return set == null ? ifaces : set.toArray(new Class[set.size()]);
    }

    public static Method getDeclaredMethod(final Class<?> cls, final String name, final Class<?> ... parameterTypes) {
        final Method[] methods = cls.getDeclaredMethods();
        for (final Method method : methods)
            if (name.equals(method.getName()) && Arrays.equals(method.getParameterTypes(), parameterTypes))
                return method;

        return null;
    }

    /**
     * 递归返回当前类的所有方法，包括父类的和私有的方法
     * @param cls
     * @param name
     * @param parameterTypes
     * @return
     */
    public static Method getDeclaredMethodDeep(Class<?> cls, String name, Class<?>... parameterTypes) {
        Method method;
        do {
            method = getDeclaredMethod(cls, name, parameterTypes);
        } while(method == null && (cls = cls.getSuperclass()) != null);

        return method;
    }
}
