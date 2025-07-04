package com.lb.rpc.reflect.javassist;

import com.lb.rpc.reflect.api.ReflectInvoker;
import com.lb.rpc.spi.annotation.SPIClass;
import javassist.util.proxy.ProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Javassist反射实现
 * <p>
 * 【技术特点】
 * Javassist是一个用于编辑Java字节码的库，可以在运行时修改类。
 * 这里通过创建代理类的方式实现方法调用。
 * <p>
 * 【注意事项】
 * 这个实现存在一个问题：它创建了新的实例而不是使用传入的serviceBean，
 * 这在有状态的服务中可能导致问题。实际应用中需要优化。
 */
@SPIClass
public class JavassistReflectInvoker implements ReflectInvoker {
    private final Logger logger = LoggerFactory.getLogger(JavassistReflectInvoker.class);

    @Override
    public Object invokeMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Throwable {
        logger.info("use javassist reflect type invoke method...");
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setSuperclass(serviceClass);   // 设置父类
        Class<?> childClass = proxyFactory.createClass();   // 生成代理类
        Method method = childClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(childClass.newInstance(), parameters);
    }
}
