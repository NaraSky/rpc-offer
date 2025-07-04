package com.lb.rpc.reflect.bytebuddy;

import com.lb.rpc.reflect.api.ReflectInvoker;
import com.lb.rpc.spi.annotation.SPIClass;
import net.bytebuddy.ByteBuddy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * ByteBuddy反射实现
 * <p>
 * 【技术优势】
 * ByteBuddy是一个现代的字节码操作库，提供了更简洁的API
 * 和更好的性能。它被广泛用于代理、Mock、AOP等场景。
 */
@SPIClass
public class ByteBuddyReflectInvoker implements ReflectInvoker {
    private final Logger logger = LoggerFactory.getLogger(ByteBuddyReflectInvoker.class);

    @Override
    public Object invokeMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Throwable {
        logger.info("use bytebuddy reflect type invoke method...");
        // 动态子类创建
        Class<?> childClass = new ByteBuddy()
                .subclass(serviceClass)  // 创建子类
                .make()                  // 生成字节码
                .load(ByteBuddyReflectInvoker.class.getClassLoader())  // 加载类
                .getLoaded();           // 获取加载的类
        Object instance = childClass.getDeclaredConstructor().newInstance();
        Method method = childClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(instance, parameters);
    }
}
