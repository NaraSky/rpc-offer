package com.lb.rpc.consumer.common;

import com.lb.rpc.consumer.common.future.RPCFuture;
import com.lb.rpc.consumer.common.handler.RpcConsumerHandler;
import com.lb.rpc.consumer.common.initializer.RpcConsumerInitializer;
import com.lb.rpc.protocol.RpcProtocol;
import com.lb.rpc.protocol.request.RpcRequest;
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
public class RpcConsumer {
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
        eventLoopGroup.shutdownGracefully();
    }

    /**
     * 发送RPC请求到服务提供者, 现在改为返回 Object，意味着要实现同步调用，等待服务提供者的响应并返回结果
     *
     * @param protocol RPC协议对象，包含请求信息
     * @throws Exception 连接或发送过程中的异常
     */
    public RPCFuture sendRequest(RpcProtocol<RpcRequest> protocol) throws Exception {
        // TODO: 暂时硬编码服务地址，后续引入注册中心时从注册中心获取服务地址
        String serviceAddress = "127.0.0.1";
        int port = 27880;

        // 构造连接键，格式为"ip_port"
        String key = serviceAddress.concat("_").concat(String.valueOf(port));

        // 从缓存中获取连接处理器
        RpcConsumerHandler handler = handlerMap.get(key);
        // 如果缓存中没有对应的处理器，创建新的连接
        if (handler == null) {
            handler = getRpcConsumerHandler(serviceAddress, port);
            handlerMap.put(key, handler);
        } else if (!handler.getChannel().isActive()) {  // 如果缓存中存在处理器但连接不活跃，重新创建连接
            handler.close();     // 关闭旧连接
            handler = getRpcConsumerHandler(serviceAddress, port); // 创建新连接
            handlerMap.put(key, handler);   // 更新缓存
        }
        RpcRequest request = protocol.getBody();
        return handler.sendRequest(protocol, request.getAsync(), request.getOneway());
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
