package com.lb.rpc.loadbalancer.hash.weight;

import com.lb.rpc.loadbalancer.api.ServiceLoadBalancer;
import com.lb.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@SPIClass
public class HashWeightServiceLoadBalancer<T> implements ServiceLoadBalancer<T> {
    private final Logger logger = LoggerFactory.getLogger(HashWeightServiceLoadBalancer.class);

    @Override
    public T select(List<T> servers, int hashCode) {
        logger.info("基于加权Hash算法的负载均衡策略...");
        if (servers == null || servers.isEmpty()) {
            return null;
        }
        hashCode = Math.abs(hashCode);
        int count = hashCode % servers.size();
        if (count <= 0) {
            count = servers.size();
        }
        int index = hashCode % count;
        return servers.get(index);
    }
}
