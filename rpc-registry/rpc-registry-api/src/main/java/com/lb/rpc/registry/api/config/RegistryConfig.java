package com.lb.rpc.registry.api.config;

/**
 * 注册中心配置类
 * 用于存储注册中心的连接地址和类型信息
 */
public class RegistryConfig {

    /**
     * 注册中心地址 (如：127.0.0.1:2181)
     */
    private String registryAddr;
    /**
     * 注册中心类型 (如：zookeeper、nacos等)
     */
    private String registryType;
    /**
     * 负载均衡类型
     */
    private String registryLoadBalanceType;

    public RegistryConfig(String registryAddr, String registryType, String registryLoadBalanceType) {
        this.registryAddr = registryAddr;
        this.registryType = registryType;
        this.registryLoadBalanceType = registryLoadBalanceType;
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

    public String getRegistryLoadBalanceType() {
        return registryLoadBalanceType;
    }

    public void setRegistryLoadBalanceType(String registryLoadBalanceType) {
        this.registryLoadBalanceType = registryLoadBalanceType;
    }
}
