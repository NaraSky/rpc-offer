package com.lb.rpc.consumer;

import com.lb.rpc.consumer.common.RpcConsumer;
import com.lb.rpc.proxy.api.ProxyFactory;
import com.lb.rpc.proxy.api.async.IAsyncObjectProxy;
import com.lb.rpc.proxy.api.config.ProxyConfig;
import com.lb.rpc.proxy.api.object.ObjectProxy;
import com.lb.rpc.proxy.jdk.JdkProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcClient {
    private final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    /**
     * 服务版本
     */
    private String serviceVersion;
    /**
     * 服务分组
     */
    private String serviceGroup;
    /**
     * 序列化类型
     */
    private String serializationType;
    /**
     * 超时时间
     */
    private long timeout;

    /**
     * 是否异步调用
     */
    private boolean async;

    /**
     * 是否单向调用
     */
    private boolean oneway;

    public RpcClient(String serviceVersion, String serviceGroup, String serializationType, long timeout, boolean async, boolean oneway) {
        this.serviceVersion = serviceVersion;
        this.timeout = timeout;
        this.serviceGroup = serviceGroup;
        this.serializationType = serializationType;
        this.async = async;
        this.oneway = oneway;
    }

    public <T> T create(Class<T> interfaceClass) {
        ProxyFactory proxyFactory = new JdkProxyFactory<>();
        proxyFactory.init(new ProxyConfig<>(interfaceClass, serviceVersion, serviceGroup, serializationType, timeout, RpcConsumer.getInstance(), async, oneway));
        return proxyFactory.getProxy(interfaceClass);
    }

    public <T> IAsyncObjectProxy createAsync(Class<T> interfaceClass) {
        return new ObjectProxy<T>(interfaceClass, serviceVersion, serviceGroup, serializationType, timeout, RpcConsumer.getInstance(), async, oneway);
    }

    public void shutdown() {
        RpcConsumer.getInstance().close();
    }
}
