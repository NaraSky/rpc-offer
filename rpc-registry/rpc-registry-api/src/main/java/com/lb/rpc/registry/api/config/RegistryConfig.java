package com.lb.rpc.registry.api.config;

/**
 * 注册中心配置类
 * 用于存储注册中心的连接地址和类型信息
 */
public class RegistryConfig {

    /** 注册中心地址 (如：127.0.0.1:2181) */
    private String registryAddr;
    /** 注册中心类型 (如：zookeeper、nacos等) */
    private String registryType;

    public RegistryConfig(String registryAddr, String registryType) {
        this.registryAddr = registryAddr;
        this.registryType = registryType;
    }

    public String getRegistryAddr() {
        return registryAddr;
    }

    public void setRegistryAddr(String registryAddr) {
        this.registryAddr = registryAddr;
    }

    public String getRegistryType() {
        return registryType;
    }

    public void setRegistryType(String registryType) {
        this.registryType = registryType;
    }
}
