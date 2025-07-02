package com.lb.rpc.registry.zookeeper;

import com.lb.rpc.common.helper.RpcServiceHelper;
import com.lb.rpc.loadbalancer.api.ServiceLoadBalancer;
import com.lb.rpc.loadbalancer.random.RandomServiceLoadBalancer;
import com.lb.rpc.protocol.meta.ServiceMeta;
import com.lb.rpc.registry.api.RegistryService;
import com.lb.rpc.registry.api.config.RegistryConfig;
import com.lb.rpc.spi.loader.ExtensionLoader;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * 基于Zookeeper的注册中心实现
 * 使用Apache Curator框架进行Zookeeper操作
 */
public class ZookeeperRegistryService implements RegistryService {

    /**
     * 重试基础等待时间(毫秒)
     */
    public static final int BASE_SLEEP_TIME_MS = 1000;
    /**
     * 最大重试次数
     */
    public static final int MAX_RETRIES = 3;
    /**
     * Zookeeper中服务注册的根路径
     */
    public static final String ZK_BASE_PATH = "/rpc-offer";

    /**
     * Curator服务发现组件，用于管理服务的注册和发现
     */
    private ServiceDiscovery<ServiceMeta> serviceDiscovery;

    //负载均衡接口
    private ServiceLoadBalancer<ServiceInstance<ServiceMeta>> serviceLoadBalancer;

    /**
     * 初始化Zookeeper注册中心
     *
     * @param registryConfig 注册中心配置
     * @throws Exception 初始化异常
     */
    @Override
    public void init(RegistryConfig registryConfig) throws Exception {
        // 创建Curator客户端，配置连接地址和重试策略
        CuratorFramework client = CuratorFrameworkFactory.newClient(registryConfig.getRegistryAddr(),
                new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, MAX_RETRIES));

        client.start();

        JsonInstanceSerializer<ServiceMeta> serializer = new JsonInstanceSerializer<>(ServiceMeta.class);
        // 构建服务发现组件
        serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceMeta.class)
                .client(client)             // 设置Zookeeper客户端
                .basePath(ZK_BASE_PATH)     // 设置服务注册根路径
                .serializer(serializer)     // 设置序列化器
                .build();
        serviceDiscovery.start();
        //TODO 默认创建基于随机算法的负载均衡策略，后续基于SPI扩展
        this.serviceLoadBalancer = ExtensionLoader.getExtension(ServiceLoadBalancer.class, registryConfig.getRegistryLoadBalanceType());
    }

    @Override
    public void register(ServiceMeta serviceMeta) throws Exception {
        ServiceInstance<ServiceMeta> serviceInstance = ServiceInstance
                .<ServiceMeta>builder()
                .name(RpcServiceHelper.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion(), serviceMeta.getServiceGroup()))
                .address(serviceMeta.getServiceAddr())
                .port(serviceMeta.getServicePort())
                .payload(serviceMeta)
                .build();
        serviceDiscovery.registerService(serviceInstance);
    }

    @Override
    public void unRegister(ServiceMeta serviceMeta) throws Exception {
        ServiceInstance<ServiceMeta> serviceInstance = ServiceInstance
                .<ServiceMeta>builder()
                .name(RpcServiceHelper.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion(), serviceMeta.getServiceGroup()))
                .address(serviceMeta.getServiceAddr())
                .port(serviceMeta.getServicePort())
                .payload(serviceMeta)
                .build();
        serviceDiscovery.unregisterService(serviceInstance);
    }

    /**
     * 服务发现 - 根据服务名查找可用服务实例
     *
     * @param serviceName     服务名称
     * @param invokerHashCode 调用方hash码(当前版本未使用)
     * @return 服务元数据，如果没有找到返回null
     * @throws Exception 发现异常
     */
    @Override
    public ServiceMeta discovery(String serviceName, int invokerHashCode) throws Exception {
        // 从Zookeeper查询指定服务名的所有实例
        Collection<ServiceInstance<ServiceMeta>> serviceInstances = serviceDiscovery.queryForInstances(serviceName);
        // 从多个实例中选择一个 (负载均衡)
        ServiceInstance<ServiceMeta> instance = serviceLoadBalancer.select((List<ServiceInstance<ServiceMeta>>) serviceInstances, invokerHashCode);
        if (instance == null) {
            return null;
        }
        return instance.getPayload();
    }

    @Override
    public void destroy() throws IOException {
        serviceDiscovery.close();
    }
}
