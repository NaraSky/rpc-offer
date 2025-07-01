package com.lb.rpc.proxy.api;

import com.lb.rpc.proxy.api.config.ProxyConfig;
import com.lb.rpc.spi.annotation.SPI;

@SPI
public interface ProxyFactory {

    /**
     * 获取代理对象
     */
    <T> T getProxy(Class<T> clazz);

    /**
     * 默认初始化方法
     */
    default <T> void init(ProxyConfig<T> proxyConfig) {
    }
}
