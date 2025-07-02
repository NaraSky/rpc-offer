package com.lb.rpc.loadbalancer.api;

import com.lb.rpc.constants.RpcConstants;
import com.lb.rpc.spi.annotation.SPI;

import java.util.List;

@SPI(RpcConstants.SERVICE_LOAD_BALANCER_RANDOM)
public interface ServiceLoadBalancer<T> {

    T select(List<T> servers, int hashcode);

}
