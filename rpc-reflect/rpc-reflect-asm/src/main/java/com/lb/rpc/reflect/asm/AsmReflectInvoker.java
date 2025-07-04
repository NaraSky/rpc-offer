package com.lb.rpc.reflect.asm;

import com.lb.rpc.reflect.api.ReflectInvoker;
import com.lb.rpc.reflect.asm.proxy.ReflectProxy;
import com.lb.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * ASM反射实现
 * <p>
 * 【技术复杂度】
 * ASM是最底层的字节码操作库，性能最好但使用最复杂。
 * 这里通过动态代理的方式实现方法调用。
 * <p>
 * 【设计模式】
 * 使用了代理模式，通过InvocationHandler处理方法调用。
 */
@SPIClass
public class AsmReflectInvoker implements ReflectInvoker {

    // 日志记录器
    private final Logger logger = LoggerFactory.getLogger(AsmReflectInvoker.class);

    /**
     * 通过ASM代理方式调用目标方法
     *
     * @param serviceBean    目标服务对象实例
     * @param serviceClass   目标服务类的Class对象
     * @param methodName     方法名
     * @param parameterTypes 方法参数类型数组
     * @param parameters     方法参数值数组
     * @return 方法调用结果
     * @throws Throwable 反射或代理调用过程中抛出的异常
     */
    @Override
    public Object invokeMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Throwable {
        logger.info("use asm reflect type invoke method...");
        // 获取无参构造方法
        Constructor<?> constructor = serviceClass.getConstructor(new Class[]{});
        Object[] constructorParam = new Object[]{};
        // 通过ASM生成代理实例，绑定InvocationHandler
        Object instance = ReflectProxy.newProxyInstance(
                AsmReflectInvoker.class.getClassLoader(),
                getInvocationHandler(serviceBean),
                serviceClass,
                constructor,
                constructorParam);
        // 获取目标方法
        Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        // 通过代理对象调用目标方法
        return method.invoke(instance, parameters);
    }

    /**
     * 获取InvocationHandler，用于代理方法调用
     *
     * @param obj 被代理的目标对象
     * @return InvocationHandler实例
     */
    private InvocationHandler getInvocationHandler(Object obj) {
        return (proxy, method, args) -> {
            logger.info("use proxy invoke method...");
            method.setAccessible(true);
            // 直接通过反射调用目标对象的方法
            Object result = method.invoke(obj, args);
            return result;
        };
    }
}
