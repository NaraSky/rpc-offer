package com.lb.rpc.proxy.api;

import com.lb.rpc.proxy.api.config.ProxyConfig;
import com.lb.rpc.proxy.api.object.ObjectProxy;

public abstract class BaseProxyFactory<T> implements ProxyFactory {

    protected ObjectProxy<T> objectProxy;

    @Override
    public <T> void init(ProxyConfig<T> proxyConfig) {
        this.objectProxy = new ObjectProxy(proxyConfig.getClazz(),
                proxyConfig.getServiceVersion(),
                proxyConfig.getServiceGroup(),
                proxyConfig.getSerializationType(),
                proxyConfig.getTimeout(),
                proxyConfig.getConsumer(),
                proxyConfig.getAsync(),
                proxyConfig.getOneway());
    }
}
