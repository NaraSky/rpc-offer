package com.lb.rpc.test.provider.service.impl;

import com.lb.rpc.annotation.RpcService;
import com.lb.rpc.test.api.DemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RpcService(interfaceClass = DemoService.class,
        interfaceClassName = "com.lb.rpc.test.api.DemoService",
        version = "1.0.0", group = "zhiyu", weight = 2)
public class ProviderDemoServiceImpl implements DemoService {

    private final Logger logger = LoggerFactory.getLogger(ProviderDemoServiceImpl.class);

    @Override
    public String hello(String name) {
        System.out.println("【服务端】调用hello方法，参数：" + name);
        logger.info("调用hello方法传入的参数为===>>>{}", name);
        return "hello " + name;
    }

}
