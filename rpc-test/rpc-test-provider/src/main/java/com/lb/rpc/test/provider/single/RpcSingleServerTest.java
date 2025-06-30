package com.lb.rpc.test.provider.single;

import com.lb.rpc.provider.RpcSingleServer;
import org.junit.Test;

/**
 * 测试Java原生启动RPC
 */
public class RpcSingleServerTest {

    @Test
    public void startRpcSingleServer() {
        RpcSingleServer singleServer = new RpcSingleServer(
                "127.0.0.1:27880", "117.72.33.162:2181", "zookeeper", "com.lb.rpc.test", "cglib");
        singleServer.startNettyServer();
    }
}
