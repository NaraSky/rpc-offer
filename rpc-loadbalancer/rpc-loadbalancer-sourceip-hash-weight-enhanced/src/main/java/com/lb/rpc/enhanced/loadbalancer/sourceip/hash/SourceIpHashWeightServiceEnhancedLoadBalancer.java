package com.lb.rpc.enhanced.loadbalancer.sourceip.hash;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@SPIClass
public class SourceIpHashWeightServiceEnhancedLoadBalancer extends BaseEnhancedServiceLoadBalancer {

    private final Logger logger = LoggerFactory.getLogger(SourceIpHashWeightServiceEnhancedLoadBalancer.class);
    @Override
    public ServiceMeta select(List<ServiceMeta> servers, int hashCode, String ip) {
        logger.info("增强型基于权重的源IP地址Hash的负载均衡策略...");
        servers = this.getWeightServiceMetaList(servers);
        if (servers == null || servers.isEmpty()){
            return null;
        }
        //传入的IP地址为空，则默认返回第一个服务实例m
        if (StringUtils.isEmpty(ip)){
            return servers.get(0);
        }
        int resultHashCode = Math.abs(ip.hashCode() + hashCode);
        return servers.get(resultHashCode % servers.size());
    }
}
