package com.lb.rpc.test.scanner.provider;

import com.lb.rpc.annotation.RpcService;
import com.lb.rpc.test.scanner.service.DemoService;

@RpcService(interfaceClass = DemoService.class,
        interfaceClassName = "com.lb.rpc.test.scanner.service.DemoService",
        version = "1.0.0",
        group = "com.lb")
public class ProviderDemoServiceImpl implements DemoService {
}
