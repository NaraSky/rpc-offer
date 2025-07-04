package com.lb.rpc.provider.common.server.base;

import com.lb.rpc.codec.RpcDecoder;
import com.lb.rpc.codec.RpcEncoder;
import com.lb.rpc.provider.common.handler.RpcProviderHandler;
import com.lb.rpc.provider.common.server.api.Server;
import com.lb.rpc.registry.api.RegistryService;
import com.lb.rpc.registry.api.config.RegistryConfig;
import com.lb.rpc.registry.zookeeper.ZookeeperRegistryService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * BaseServer 是一个通用的 RPC 服务端实现，基于 Netty 搭建网络通信，
 * 并通过注册中心（如 Zookeeper）完成服务注册。
 */
public class BaseServer implements Server {
    private final Logger logger = LoggerFactory.getLogger(BaseServer.class);
    //主机域名或者IP地址
    protected String host = "127.0.0.1";
    //端口号
    protected int port = 27110;
    // 存放接口实现实例的映射：key 为接口全限定名，value 为对应实现对象
    protected Map<String, Object> handlerMap = new HashMap<>();
    // 用于反射调用的类型信息（例如 JDK 动态代理、CGLIB 等）
    private String reflectType;
    // 注册中心服务，用于将本服务信息注册到注册中心
    protected RegistryService registryService;

    public BaseServer(String serverAddress, String registryAddress, String registryType, String registryLoadBalanceType, String reflectType) {
        if (!StringUtils.isEmpty(serverAddress)) {
            String[] serverArray = serverAddress.split(":");
            this.host = serverArray[0];
            this.port = Integer.parseInt(serverArray[1]);
        }
        this.reflectType = reflectType;
        this.registryService = this.getRegistryService(registryAddress, registryType, registryLoadBalanceType);
    }

    /**
     * 根据配置创建具体的 RegistryService 实例
     * 目前只支持 Zookeeper，后续可扩展 SPI 加载其他实现
     */
    private RegistryService getRegistryService(String registryAddress, String registryType, String registryLoadBalanceType) {
        RegistryService registryService = null;
        try {
            // todo 后续可通过 SPI 扩展支持其他注册中心。
            registryService = new ZookeeperRegistryService();
            registryService.init(new RegistryConfig(registryAddress, registryType, registryLoadBalanceType));
        } catch (Exception e) {
            logger.error("RPC Server init error", e);
        }
        return registryService;
    }

    @Override
    public void startNettyServer() {
        // bossGroup 负责接收连接，workerGroup 负责处理 I/O
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline()
                                    // 1. 解码器：将字节流解码成 RPC 请求对象
                                    .addLast(new RpcDecoder())
                                    // 2. 编码器：将 RPC 响应对象编码成字节流
                                    .addLast(new RpcEncoder())
                                    // 3. 服务端处理器：根据请求调用本地服务实现并返回结果
                                    .addLast(new RpcProviderHandler(reflectType, handlerMap));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)      // 设置 TCP 层面参数：等待队列大小
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // 设置子通道参数：保持连接活跃
            ChannelFuture future = bootstrap.bind(host, port).sync();       // 绑定 host 和 port 并同步等待绑定完成
            logger.info("Server started on {}:{}", host, port);
            future.channel().closeFuture().sync();      // 阻塞当前线程，直到服务器 channel 关闭
        } catch (Exception e) {
            logger.error("RPC Server start error", e);
        } finally {
            // 优雅关闭线程池，释放资源
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
