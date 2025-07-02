package com.lb.rpc.proxy.bytebuddy;

import com.lb.rpc.proxy.api.BaseProxyFactory;
import com.lb.rpc.proxy.api.ProxyFactory;
import com.lb.rpc.spi.annotation.SPIClass;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SPIClass
public class ByteBuddyProxyFactory<T> extends BaseProxyFactory<T> implements ProxyFactory {
    private final Logger logger = LoggerFactory.getLogger(ByteBuddyProxyFactory.class);

    @Override
    public <T> T getProxy(Class<T> clazz) {
        try {
            logger.info("基于ByteBuddy动态代理...");
            return (T) new ByteBuddy().subclass(Object.class)
                    .implement(clazz)
                    .intercept(InvocationHandlerAdapter.of(objectProxy))
                    .make()
                    .load(ByteBuddyProxyFactory.class.getClassLoader())
                    .getLoaded()
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (Exception e) {
            logger.error("bytebuddy proxy throws exception:{}", e);
        }
        return null;
    }
}
