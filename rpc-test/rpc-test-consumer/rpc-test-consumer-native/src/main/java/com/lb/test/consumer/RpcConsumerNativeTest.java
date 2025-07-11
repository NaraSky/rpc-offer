package com.lb.test.consumer;

import com.lb.rpc.consumer.RpcClient;
import com.lb.rpc.proxy.api.async.IAsyncObjectProxy;
import com.lb.rpc.proxy.api.future.RPCFuture;
import com.lb.rpc.test.api.DemoService;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcConsumerNativeTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcConsumerNativeTest.class);

    public static void main(String[] args) {
        RpcClient rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper", "random", "jdk", "1.0.0", "zhiyu", "hessian2", 3000, false, false);
        DemoService demoService = rpcClient.create(DemoService.class);
        String result = demoService.hello("zhiyu");
        LOGGER.info("返回的结果数据===>>> " + result);
        rpcClient.shutdown();
    }

    private RpcClient rpcClient;

    @Before
    public void initRpcClient() {
        // rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper", "1.0.0", "zhiyu", "jdk", 3000, false, false);
        // rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper", "1.0.0", "zhiyu", "json", 3000, false, false);
        // rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper", "1.0.0", "zhiyu", "hessian2", 3000, false, false);
        // rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper", "1.0.0", "zhiyu", "fst", 3000, false, false);
        // rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper", "1.0.0", "zhiyu", "kryo", 3000, false, false);
        // rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper", "1.0.0", "zhiyu", "protostuff", 3000, false, false);
        // rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper", "jdk","1.0.0", "zhiyu", "protostuff", 3000, false, false);
        // rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper", "cglib","1.0.0", "zhiyu", "protostuff", 3000, false, false);
        // rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper", "javassist","1.0.0", "zhiyu", "protostuff", 3000, false, false);
        // rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper", "bytebuddy","1.0.0", "zhiyu", "protostuff", 3000, false, false);
        // rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper", "asm","1.0.0", "zhiyu", "protostuff", 3000, false, false);
        // rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper", "random","asm","1.0.0", "zhiyu", "protostuff", 3000, false, false);
        // rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper", "randomweight","asm","1.0.0", "zhiyu", "protostuff", 3000, false, false);
        // rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper", "robin","asm","1.0.0", "zhiyu", "protostuff", 3000, false, false);
        // rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper", "robinweight","asm","1.0.0", "zhiyu", "protostuff", 3000, false, false);
        // rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper", "hash","asm","1.0.0", "zhiyu", "protostuff", 3000, false, false);
        // rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper", "hashweight","asm","1.0.0", "zhiyu", "protostuff", 3000, false, false);
        // rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper", "iphash","asm","1.0.0", "zhiyu", "protostuff", 3000, false, false);
        // rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper", "iphashweight","asm","1.0.0", "zhiyu", "protostuff", 3000, false, false);
        // rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper", "zkconsistenthash","asm","1.0.0", "zhiyu", "protostuff", 3000, false, false);
        // rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper", "enhanced_randomweight","asm","1.0.0", "zhiyu", "protostuff", 3000, false, false);
        // rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper", "enhanced_robinweight","asm","1.0.0", "zhiyu", "protostuff", 3000, false, false);
        // rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper", "enhanced_hashweight","asm","1.0.0", "zhiyu", "protostuff", 3000, false, false);
        // rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper", "enhanced_iphashweight","asm","1.0.0", "zhiyu", "protostuff", 3000, false, false);
        // rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper", "enhanced_zkconsistenthash","asm","1.0.0", "zhiyu", "protostuff", 3000, false, false);
        rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper", "enhanced_leastconnections","asm","1.0.0", "zhiyu", "protostuff", 3000, false, false);
    }

    @Test
    public void testInterfaceRpc() {
        DemoService demoService = rpcClient.create(DemoService.class);
        String result = demoService.hello("zhiyu");
        LOGGER.info("返回的结果数据===>>> " + result);
        rpcClient.shutdown();
    }

    @Test
    public void testAsyncInterfaceRpc() throws Exception {
        IAsyncObjectProxy demoService = rpcClient.createAsync(DemoService.class);
        RPCFuture future = demoService.call("hello", "zhiyu");
        LOGGER.info("返回的结果数据===>>> " + future.get());
        rpcClient.shutdown();
    }
}
