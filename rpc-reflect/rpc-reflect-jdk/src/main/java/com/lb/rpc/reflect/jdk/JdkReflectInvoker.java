package com.lb.rpc.reflect.jdk;

import com.lb.rpc.reflect.api.ReflectInvoker;
import com.lb.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * JDK原生反射实现
 * <p>
 * 【性能特征】
 * - 优点：JDK原生支持，兼容性最好，无额外依赖
 * - 缺点：性能相对较低，尤其是频繁调用时
 * - 适用场景：对性能要求不高，追求稳定性的场景
 */
@SPIClass
public class JdkReflectInvoker implements ReflectInvoker {

    private final Logger logger = LoggerFactory.getLogger(JdkReflectInvoker.class);

    @Override
    public Object invokeMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Throwable {
        logger.info("use jdk reflect type invoke method...");
        Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(serviceBean, parameters);
    }
}
