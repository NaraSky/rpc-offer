package com.lb.rpc.enhanced.loadbalancer.consistenthash;

import com.lb.rpc.loadbalancer.api.ServiceLoadBalancer;
import com.lb.rpc.protocol.meta.ServiceMeta;
import com.lb.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@SPIClass
public class ZKConsistentHashEnahncedLoadBalancer implements ServiceLoadBalancer<ServiceMeta> {

    private final static int VIRTUAL_NODE_SIZE = 10;
    private final static String VIRTUAL_NODE_SPLIT = "#";

    private final Logger logger = LoggerFactory.getLogger(ZKConsistentHashEnahncedLoadBalancer.class);

    @Override
    public ServiceMeta select(List<ServiceMeta> servers, int hashCode, String ip) {
        logger.info("基于Zookeeper增强型的一致性Hash算法的负载均衡策略...");
        TreeMap<Integer, ServiceMeta> ring = makeConsistentHashRing(servers);
        return allocateNode(ring, hashCode);
    }

    private ServiceMeta allocateNode(TreeMap<Integer, ServiceMeta> ring, int hashCode) {
        Map.Entry<Integer, ServiceMeta> entry = ring.ceilingEntry(hashCode);
        if (entry == null) {
            entry = ring.firstEntry();
        }
        if (entry == null){
            throw new RuntimeException("not discover useful service, please register service in registry center.");
        }
        return entry.getValue();
    }

    private TreeMap<Integer, ServiceMeta> makeConsistentHashRing(List<ServiceMeta> servers) {
        TreeMap<Integer, ServiceMeta> ring = new TreeMap<>();
        for (ServiceMeta instance : servers) {
            for (int i = 0; i < VIRTUAL_NODE_SIZE; i++) {
                ring.put((buildServiceInstanceKey(instance) + VIRTUAL_NODE_SPLIT + i).hashCode(), instance);
            }
        }
        return ring;
    }

    private String buildServiceInstanceKey(ServiceMeta instance) {
        return String.join(":", instance.getServiceAddr(), String.valueOf(instance.getServicePort()));
    }
}
