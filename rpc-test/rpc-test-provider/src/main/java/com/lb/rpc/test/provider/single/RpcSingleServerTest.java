package com.lb.rpc.test.provider.single;

import com.lb.rpc.provider.RpcSingleServer;
import org.junit.Test;

/**
 * 测试Java原生启动RPC
 */
public class RpcSingleServerTest {

    @Test
    public void startRpcSingleServer() {
        RpcSingleServer singleServer = new RpcSingleServer("127.0.0.1:27880", "127.0.0.1:2181", "zookeeper", "com.lb.rpc.test", "asm");
        singleServer.startNettyServer();
    }
}
