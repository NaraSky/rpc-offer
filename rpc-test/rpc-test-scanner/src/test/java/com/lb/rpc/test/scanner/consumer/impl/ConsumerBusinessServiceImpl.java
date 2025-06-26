package com.lb.rpc.test.scanner.consumer.impl;

import com.lb.rpc.annotation.RpcReference;
import com.lb.rpc.test.scanner.consumer.ConsumerBusinessService;
import com.lb.rpc.test.scanner.service.DemoService;

public class ConsumerBusinessServiceImpl implements ConsumerBusinessService {

    @RpcReference(registryType = "zookeeper", registryAddress = "127.0.0.1:2181", version = "1.0.0", group = "com.lb")
    private DemoService demoService;

}
