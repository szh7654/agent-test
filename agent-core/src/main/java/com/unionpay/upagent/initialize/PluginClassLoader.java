//package com.unionpay.upagent.initialize;
//
//import java.io.File;
//import java.net.URL;
//import java.net.URLClassLoader;
//
//public class PluginClassLoader extends URLClassLoader {
//
//    File jarFile;
//    public PluginClassLoader(URL[] urls, ClassLoader parent, File jarFile) {
//        super(urls, parent);
//        this.jarFile = jarFile;
//    }
//
//    @Override
//    protected Class<?> findClass(String name) throws ClassNotFoundException {
//        String path = name.replace('.', '/').concat(".class");
//        byte[] bytes = loadClassData(path);
//        return defineClass(name, bytes, 0, bytes.length);
//    }
//
//    private byte[] loadClassData(String path) {
//
//    }
//}
