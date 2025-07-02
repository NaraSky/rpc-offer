package com.lb.rpc.reflect.asm;

import com.lb.rpc.reflect.api.ReflectInvoker;
import com.lb.rpc.reflect.asm.proxy.ReflectProxy;
import com.lb.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@SPIClass
public class AsmReflectInvoker implements ReflectInvoker {

    private final Logger logger = LoggerFactory.getLogger(AsmReflectInvoker.class);

    @Override
    public Object invokeMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Throwable {
        logger.info("use asm reflect type invoke method...");
        Constructor<?> constructor = serviceClass.getConstructor(new Class[]{});
        Object[] constructorParam = new Object[]{};
        Object instance = ReflectProxy.newProxyInstance(AsmReflectInvoker.class.getClassLoader(), getInvocationHandler(serviceBean), serviceClass, constructor, constructorParam);
        Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(instance, parameters);
    }

    private InvocationHandler getInvocationHandler(Object obj) {
        return (proxy, method, args) -> {
            logger.info("use proxy invoke method...");
            method.setAccessible(true);
            Object result = method.invoke(obj, args);
            return result;
        };
    }
}
