package com.lb.rpc.consumer.common;

import com.lb.rpc.common.helper.RpcServiceHelper;
import com.lb.rpc.common.threadpool.ClientThreadPool;
import com.lb.rpc.consumer.common.handler.RpcConsumerHandler;
import com.lb.rpc.consumer.common.handler.RpcConsumerHandlerHelper;
import com.lb.rpc.consumer.common.initializer.RpcConsumerInitializer;
import com.lb.rpc.protocol.RpcProtocol;
import com.lb.rpc.protocol.meta.ServiceMeta;
import com.lb.rpc.protocol.request.RpcRequest;
import com.lb.rpc.proxy.api.consumer.Consumer;
import com.lb.rpc.proxy.api.future.RPCFuture;
import com.lb.rpc.registry.api.RegistryService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RPC消费者客户端
 * 负责与RPC服务提供者建立连接，发送请求并接收响应
 * 采用单例模式，支持连接复用和管理
 */
public class RpcConsumer implements Consumer {
    private final Logger logger = LoggerFactory.getLogger(RpcConsumer.class);

    // Netty客户端启动器，用于配置和启动客户端
    private final Bootstrap bootstrap;

    // Netty事件循环组，处理网络事件
    private final EventLoopGroup eventLoopGroup;

    // 单例实例，使用volatile保证可见性
    private static volatile RpcConsumer instance;

    // 连接处理器缓存映射，key为"ip_port"格式，value为对应的连接处理器
    // 使用ConcurrentHashMap保证线程安全
    private static Map<String, RpcConsumerHandler> handlerMap = new ConcurrentHashMap<>();

    private RpcConsumer() {
        bootstrap = new Bootstrap();
        // 创建NIO事件循环组，线程数为
        eventLoopGroup = new NioEventLoopGroup(4);
        // 配置Bootstrap：设置事件循环组、Channel类型和初始化器
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)    // 使用NIO Socket Channel
                .handler(new RpcConsumerInitializer()); // 设置Channel初始化器
    }

    public static RpcConsumer getInstance() {
        if (instance == null) {
            synchronized (RpcConsumer.class) {
                if (instance == null) {
                    instance = new RpcConsumer();
                }
            }
        }
        return instance;
    }

    /**
     * 关闭RPC消费者，释放资源
     * 优雅关闭事件循环组，等待所有任务完成
     */
    public void close() {
        RpcConsumerHandlerHelper.closeRpcClientHandler();
        eventLoopGroup.shutdownGracefully();
        ClientThreadPool.shutdown();
    }

    /**
     * 发送RPC请求到服务提供者, 现在改为返回 Object，意味着要实现同步调用，等待服务提供者的响应并返回结果
     *
     * @param protocol RPC协议对象，包含请求信息
     * @throws Exception 连接或发送过程中的异常
     */
    @Override
    public RPCFuture sendRequest(RpcProtocol<RpcRequest> protocol, RegistryService registryService) throws Exception {
        RpcRequest request = protocol.getBody();
        String serviceKey = RpcServiceHelper.buildServiceKey(request.getClassName(), request.getVersion(), request.getGroup());
        Object[] params = request.getParameters();
        int invokerHashCode = (params == null || params.length <= 0) ? serviceKey.hashCode() : params[0].hashCode();
        ServiceMeta serviceMeta = registryService.discovery(serviceKey, invokerHashCode);
        if (serviceMeta != null) {
            RpcConsumerHandler handler = RpcConsumerHandlerHelper.get(serviceMeta);
            if (handler == null || handler.getChannel() == null || !handler.getChannel().isActive()) {
                handler = getRpcConsumerHandler(serviceMeta.getServiceAddr(), serviceMeta.getServicePort());
                RpcConsumerHandlerHelper.put(serviceMeta, handler);
            }
            return handler.sendRequest(protocol, request.getAsync(), request.getOneway());
        }
        return null;
    }

    /**
     * 创建到指定服务地址的连接并返回对应的处理器
     *
     * @param serviceAddress 服务提供者IP地址
     * @param port           服务提供者端口号
     * @return RpcConsumerHandler 连接处理器
     * @throws InterruptedException 连接过程被中断时抛出
     */
    private RpcConsumerHandler getRpcConsumerHandler(String serviceAddress, int port) throws InterruptedException {
        System.out.println("【客户端】即将连接服务端：" + serviceAddress + ":" + port);
        // 发起连接并同步等待连接完成
        ChannelFuture channelFuture = bootstrap.connect(serviceAddress, port).sync();

        // 添加连接完成监听器，处理连接成功或失败的情况
        channelFuture.addListener((ChannelFutureListener) listener -> {
            if (channelFuture.isSuccess()) {
                // 连接成功，记录日志
                logger.info("connect rpc server {} on port {} success.", serviceAddress, port);
            } else {
                // 连接失败，记录错误日志并关闭资源
                logger.error("connect rpc server {} on port {} failed.", serviceAddress, port);
                channelFuture.cause().printStackTrace();
                eventLoopGroup.shutdownGracefully();
            }
        });
        // 从Channel的Pipeline中获取RpcConsumerHandler实例并返回
        return channelFuture.channel().pipeline().get(RpcConsumerHandler.class);
    }
}
