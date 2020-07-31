package com.netty.rpc.consumer.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class RpcProxy {
    public static <T> T createProxy(Class<T> clazz){
        return Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new MethodProxy(clazz));
    }
}
