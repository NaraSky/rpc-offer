package com.lb.rpc.loadbalancer.api;

import java.util.List;

public interface ServiceLoadBalancer<T> {

    T select(List<T> servers, int hashcode);

}
