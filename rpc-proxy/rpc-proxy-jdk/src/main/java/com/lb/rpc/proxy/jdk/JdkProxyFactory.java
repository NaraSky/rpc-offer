package com.lb.rpc.proxy.jdk;

import com.lb.rpc.proxy.api.BaseProxyFactory;
import com.lb.rpc.proxy.api.ProxyFactory;
import com.lb.rpc.proxy.api.consumer.Consumer;
import com.lb.rpc.proxy.api.object.ObjectProxy;

import java.lang.reflect.Proxy;

public class JdkProxyFactory<T> extends BaseProxyFactory<T> implements ProxyFactory {

    @Override
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[]{clazz},
                objectProxy
        );
    }
}
