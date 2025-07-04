package com.lb.rpc.loadbalancer.helper;

import com.lb.rpc.protocol.meta.ServiceMeta;
import org.apache.curator.x.discovery.ServiceInstance;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServiceLoadBalancerHelper {

    private static volatile List<ServiceMeta> cacheServiceMeta = new CopyOnWriteArrayList<>();

    public static List<ServiceMeta> getServiceMetaList(List<ServiceInstance<ServiceMeta>> serviceInstances) {
        if (serviceInstances == null || serviceInstances.isEmpty() || cacheServiceMeta.size() == serviceInstances.size()) {
            return cacheServiceMeta;
        }
        //先清空cacheServiceMeta中的数据
        cacheServiceMeta.clear();
        serviceInstances.stream().forEach((serviceMetaServiceInstance) -> {
            cacheServiceMeta.add(serviceMetaServiceInstance.getPayload());
        });
        return cacheServiceMeta;
    }

}
