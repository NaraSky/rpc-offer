package com.lb.rpc.proxy.jdk;

import com.lb.rpc.proxy.api.BaseProxyFactory;
import com.lb.rpc.proxy.api.ProxyFactory;
import com.lb.rpc.proxy.api.consumer.Consumer;
import com.lb.rpc.proxy.api.object.ObjectProxy;
import com.lb.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;

@SPIClass
public class JdkProxyFactory<T> extends BaseProxyFactory<T> implements ProxyFactory {
    private final Logger logger = LoggerFactory.getLogger(JdkProxyFactory.class);

    @Override
    public <T> T getProxy(Class<T> clazz) {
        logger.info("基于JDK动态代理...");
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[]{clazz},
                objectProxy
        );
    }
}
