package com.lb.rpc.spi.factory;

import com.lb.rpc.spi.annotation.SPI;
import com.lb.rpc.spi.loader.ExtensionLoader;

import java.util.Optional;

/**
 * SPI扩展工厂的默认实现
 * 使用ExtensionLoader来获取SPI扩展实例
 * <p>
 * 这个类实现了ExtensionFactory接口，提供了通过SPI机制获取扩展实例的功能
 */
public class SpiExtensionFactory implements ExtensionFactory {
    /**
     * 获取SPI扩展实例
     * 使用函数式编程风格，通过Optional链式调用来安全地获取扩展实例
     *
     * @param key   扩展名称
     * @param clazz 要获取的扩展接口类型
     * @param <T>   泛型类型
     * @return 扩展实例，如果获取失败返回null
     */
    @Override
    public <T> T getExtension(String key, Class<T> clazz) {
        return Optional.ofNullable(clazz)                           // 1. 检查clazz是否为null
                .filter(Class::isInterface)                         // 2. 过滤：必须是接口
                .filter(cls -> cls.isAnnotationPresent(SPI.class))  // 3. 过滤：必须有@SPI注解
                .map(ExtensionLoader::getExtensionLoader)           // 4. 获取对应的ExtensionLoader
                .map(ExtensionLoader::getDefaultSpiClassInstance)   // 5. 获取默认的SPI实现实例
                .orElse(null);                                      // 6. 如果任何一步失败，返回null
    }
}
