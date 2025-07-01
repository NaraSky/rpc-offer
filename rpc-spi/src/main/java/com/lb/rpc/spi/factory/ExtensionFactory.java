package com.lb.rpc.spi.factory;

import com.lb.rpc.spi.annotation.SPI;

/**
 * 扩展工厂接口
 * 负责创建和获取SPI扩展实例
 * <p>
 * 使用@SPI注解标记，默认实现为"spi"
 * 这个接口本身也是通过SPI机制来扩展的
 */
@SPI("spi")
public interface ExtensionFactory {

    /**
     * 获取扩展实例
     *
     * @param key   扩展名称
     * @param clazz 要获取的扩展接口类型
     * @param <T>   泛型类型
     * @return 扩展实例，如果获取失败返回null
     */
    <T> T getExtension(String key, Class<T> clazz);

}
