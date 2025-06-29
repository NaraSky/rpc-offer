package com.lb.rpc.test.consumer.handler;

import com.lb.rpc.consumer.common.RpcConsumer;
import com.lb.rpc.protocol.RpcProtocol;
import com.lb.rpc.protocol.header.RpcHeaderFactory;
import com.lb.rpc.protocol.request.RpcRequest;

public class RpcConsumerHandlerTest {

    public static void main(String[] args) throws Exception {
        RpcConsumer consumer = RpcConsumer.getInstance();
        consumer.sendRequest(getRpcRequestProtocol());
        Thread.sleep(2000);
        consumer.close();
    }

    private static RpcProtocol<RpcRequest> getRpcRequestProtocol(){
        //模拟发送数据
        RpcProtocol<RpcRequest> protocol = new RpcProtocol<RpcRequest>();
        protocol.setHeader(RpcHeaderFactory.getRequestHeader("jdk"));
        RpcRequest request = new RpcRequest();
        request.setClassName("com.lb.rpc.test.api.DemoService");
        request.setGroup("zhiyu");
        request.setMethodName("hello");
        request.setParameters(new Object[]{"zhiyu"});
        request.setParameterTypes(new Class[]{String.class});
        request.setVersion("1.0.0");
        request.setAsync(false);
        request.setOneway(false);
        protocol.setBody(request);
        return protocol;
    }
}
