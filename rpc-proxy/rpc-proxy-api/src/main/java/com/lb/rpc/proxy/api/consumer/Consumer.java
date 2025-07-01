package com.lb.rpc.proxy.api.consumer;

import com.lb.rpc.protocol.RpcProtocol;
import com.lb.rpc.protocol.request.RpcRequest;
import com.lb.rpc.proxy.api.future.RPCFuture;
import com.lb.rpc.registry.api.RegistryService;

/**
 * 发送请求并返回一个Future对象。这个接口的实现类负责实际的网络通信。
 */
public interface Consumer {

    /**
     * 消费者发送 request 请求
     */
    RPCFuture sendRequest(RpcProtocol<RpcRequest> protocol, RegistryService registryService) throws Exception;
}
