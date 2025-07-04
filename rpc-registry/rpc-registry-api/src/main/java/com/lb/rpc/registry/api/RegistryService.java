package com.lb.rpc.registry.api;

import com.lb.rpc.protocol.meta.ServiceMeta;
import com.lb.rpc.registry.api.config.RegistryConfig;
import com.lb.rpc.spi.annotation.SPI;

import java.io.IOException;

/**
 * 注册中心服务接口
 * 定义了服务注册、发现、注销等核心功能
 */
@SPI
public interface RegistryService {

    void register(ServiceMeta serviceMeta) throws Exception;

    void unRegister(ServiceMeta serviceMeta) throws Exception;

    /**
     * 服务发现
     *
     * @param serviceName     服务名称
     * @param invokerHashCode HashCode值
     * @param sourceIp        源IP地址
     * @return 服务元数据
     * @throws Exception 抛出异常
     */
    ServiceMeta discovery(String serviceName, int invokerHashCode, String sourceIp) throws Exception;

    void destroy() throws IOException;

    default void init(RegistryConfig registryConfig) throws Exception {

    }
}
