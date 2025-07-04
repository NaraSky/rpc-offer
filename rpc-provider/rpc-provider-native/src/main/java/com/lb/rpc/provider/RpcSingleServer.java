package com.lb.rpc.provider;

import com.lb.rpc.provider.common.scanner.RpcServiceScanner;
import com.lb.rpc.provider.common.server.base.BaseServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RpcSingleServer——以单实例、Java原生方式启动一个 RPC 服务
 * 继承自 BaseServer，负责扫描服务实现并注册到注册中心，再启动 Netty 服务
 */
public class RpcSingleServer extends BaseServer {
    private final Logger logger = LoggerFactory.getLogger(RpcSingleServer.class);

    /**
     * 构造方法：
     * 1. 调用父类构造，完成注册中心配置和 Netty Server 基础框架初始化
     * 2. 扫描指定包下的 @RpcService 注解类，注册服务并填充 handlerMap
     *
     * @param serverAddress           服务地址（host:port）
     * @param registryAddress         注册中心地址
     * @param registryType            注册中心类型（如 zk）
     * @param registryLoadBalanceType 负载均衡算法类型
     * @param scanPackage             要扫描的服务实现包路径
     * @param reflectType             反射调用类型（JDK/CGLIB）
     */
    public RpcSingleServer(String serverAddress, String registryAddress, String registryType, String registryLoadBalanceType, String scanPackage, String reflectType) {
        // 1. 调用 BaseServer 构造，解析地址并初始化 registryService
        super(serverAddress, registryAddress, registryType, registryLoadBalanceType, reflectType);
        try {
            // 2. 扫描服务实现，返回 key->实现实例映射，并注册到注册中心
            this.handlerMap = RpcServiceScanner.doScannerWithRpcServiceAnnotationFilterAndRegistryService(
                    this.host,
                    this.port,
                    scanPackage,
                    registryService);
        } catch (Exception e) {
            logger.error("RPC Server init error", e);
        }
    }
}
