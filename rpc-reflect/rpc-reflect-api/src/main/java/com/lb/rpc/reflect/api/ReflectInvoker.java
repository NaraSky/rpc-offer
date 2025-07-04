package com.lb.rpc.reflect.api;

import com.lb.rpc.spi.annotation.SPI;

/**
 * 反射调用器SPI接口
 * <p>
 * 【SPI设计模式】
 * SPI (Service Provider Interface) 是一种服务发现机制，
 * 允许在运行时动态加载不同的实现。这里用于支持多种反射实现，
 * 每种实现都有其特定的优势和使用场景。
 */
@SPI
public interface ReflectInvoker {

    /**
     * 调用真实方法的SPI通用接口
     *
     * @param serviceBean    方法所在的对象实例
     * @param serviceClass   方法所在对象实例的Class对象
     * @param methodName     方法的名称
     * @param parameterTypes 方法的参数类型数组
     * @param parameters     方法的参数数组
     * @return 方法调用的结果信息
     * @throws Throwable 抛出的异常
     */
    Object invokeMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Throwable;
}
