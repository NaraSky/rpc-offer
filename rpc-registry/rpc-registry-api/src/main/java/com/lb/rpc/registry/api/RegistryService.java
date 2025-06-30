package com.lb.rpc.registry.api;

import com.lb.rpc.protocol.meta.ServiceMeta;
import com.lb.rpc.registry.api.config.RegistryConfig;

import java.io.IOException;

/**
 * 注册中心服务接口
 * 定义了服务注册、发现、注销等核心功能
 */
public interface RegistryService {

    void register(ServiceMeta serviceMeta) throws Exception;

    void unRegister(ServiceMeta serviceMeta) throws Exception;

    /**
     * 服务发现
     * 根据服务名称从注册中心查找可用的服务实例
     *
     * @param serviceName     服务名称
     * @param invokerHashCode 调用方hash码，可用于负载均衡
     * @return 服务元数据信息
     * @throws Exception 发现异常
     */
    ServiceMeta discovery(String serviceName, int invokerHashCode) throws Exception;

    void destroy() throws IOException;

    default void init(RegistryConfig registryConfig) throws Exception {

    }
}
