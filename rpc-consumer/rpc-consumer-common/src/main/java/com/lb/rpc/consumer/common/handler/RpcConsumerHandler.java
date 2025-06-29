package com.lb.rpc.consumer.common.handler;

import com.alibaba.fastjson.JSONObject;
import com.lb.rpc.protocol.RpcProtocol;
import com.lb.rpc.protocol.header.RpcHeader;
import com.lb.rpc.protocol.request.RpcRequest;
import com.lb.rpc.protocol.response.RpcResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RPC消费者处理器
 * 继承SimpleChannelInboundHandler，专门处理RpcProtocol<RpcResponse>类型的消息
 * 负责管理客户端连接、发送请求和接收响应
 */
public class RpcConsumerHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {

    private final Logger logger = LoggerFactory.getLogger(RpcConsumerHandler.class);
    // 当前连接的Channel，使用volatile保证可见性
    private volatile Channel channel;
    // 远程服务提供者的地址信息
    private SocketAddress remotePeer;

    //存储请求ID与RpcResponse协议的映射关系
    private Map<Long, RpcProtocol<RpcResponse>> pendingResponse = new ConcurrentHashMap<>();

    public Channel getChannel() {
        return channel;
    }

    public SocketAddress getRemotePeer() {
        return remotePeer;
    }

    /**
     * Channel激活时的回调方法
     * 当连接建立成功时被调用，记录远程地址信息
     *
     * @param ctx ChannelHandlerContext上下文
     * @throws Exception 处理过程中的异常
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.remotePeer = this.channel.remoteAddress();
    }

    /**
     * Channel注册时的回调方法
     * 当Channel注册到EventLoop时被调用，保存Channel引用
     *
     * @param ctx ChannelHandlerContext上下文
     * @throws Exception 处理过程中的异常
     */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    /**
     * 处理接收到的RPC响应消息
     * 当从服务提供者接收到响应时被调用
     *
     * @param ctx      ChannelHandlerContext上下文
     * @param protocol 接收到的RPC协议响应对象
     * @throws Exception 处理过程中的异常
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcResponse> protocol) throws Exception {
        if (protocol == null) {
            return;
        }
        logger.info("服务消费者接收到的数据===>>>{}", JSONObject.toJSONString(protocol));

        RpcHeader header = protocol.getHeader();
        long requestId = header.getRequestId();
        pendingResponse.put(requestId, protocol);
    }

    /**
     * 服务消费者向服务提供者发送RPC请求
     *
     * @param protocol RPC协议请求对象，包含调用信息
     */
    public Object sendRequest(RpcProtocol<RpcRequest> protocol) {
        logger.info("服务消费者发送的数据===>>>{}", JSONObject.toJSONString(protocol));

        // 通过Channel发送请求数据，writeAndFlush确保数据立即发送
        channel.writeAndFlush(protocol);
        RpcHeader header = protocol.getHeader();
        long requestId = header.getRequestId();
        while (true) {
            RpcProtocol<RpcResponse> responseRpcProtocol = pendingResponse.remove(requestId);
            if (responseRpcProtocol != null) {
                return responseRpcProtocol.getBody().getResult();
            }
        }
    }

    /**
     * 关闭连接
     * 发送一个空的Buffer并在发送完成后关闭连接
     * 这是Netty推荐的优雅关闭连接的方式
     */
    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
}
