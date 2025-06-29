package com.lb.rpc.provider.common.handler;

import com.lb.rpc.common.helper.RpcServiceHelper;
import com.lb.rpc.common.threadpool.ServerThreadPool;
import com.lb.rpc.protocol.RpcProtocol;
import com.lb.rpc.protocol.enumeration.RpcStatus;
import com.lb.rpc.protocol.enumeration.RpcType;
import com.lb.rpc.protocol.header.RpcHeader;
import com.lb.rpc.protocol.request.RpcRequest;
import com.lb.rpc.protocol.response.RpcResponse;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * RPC服务提供者的Handler处理类
 * 继承自SimpleChannelInboundHandler，用于处理入站的RPC请求
 * <p>
 * 1. SimpleChannelInboundHandler是一个特殊的入站处理器，会自动释放消息对象
 * 2. 泛型参数<RpcProtocol<RpcRequest>>指定了处理的消息类型
 * 3. 只有匹配的消息类型才会被channelRead0方法处理
 */
public class RpcProviderHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {
    private final Logger logger = LoggerFactory.getLogger(RpcProviderHandler.class);

    /**
     * 服务处理器映射表
     * key: 服务标识(className:version:group格式)
     * value: 具体的服务实现对象
     */
    private final Map<String, Object> handlerMap;

    public RpcProviderHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    /**
     * 处理入站消息的核心方法
     * 当接收到RpcProtocol<RpcRequest>类型的消息时，此方法会被自动调用
     * <p>
     * 1. channelRead0是SimpleChannelInboundHandler的抽象方法，必须实现
     * 2. 该方法在Netty的EventLoop线程中执行
     * 3. 为避免阻塞EventLoop，将业务逻辑提交到线程池中异步执行
     *
     * @param ctx      Netty通道处理器上下文，用于读写数据和管理通道
     * @param protocol 接收到的RPC协议对象，包含请求头和请求体
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) throws Exception {
        // 将请求处理逻辑提交到线程池，避免阻塞Netty的EventLoop线程
        // 这是Netty最佳实践：耗时操作应该异步执行，保持EventLoop的高效运转
        ServerThreadPool.submit(() -> {
            // 获取请求头，并设置响应类型
            RpcHeader header = protocol.getHeader();
            header.setMsgType((byte) RpcType.RESPONSE.getType());

            // 获取请求体
            RpcRequest request = protocol.getBody();
            logger.debug("Receive request " + header.getRequestId());

            // 创建响应协议对象和响应体
            RpcProtocol<RpcResponse> rpcResponseRpcProtocol = new RpcProtocol<>();
            RpcResponse response = new RpcResponse();

            try {
                // 处理RPC请求，通过反射调用目标方法
                Object result = handle(request);

                // 设置响应结果
                response.setResult(result);
                response.setAsync(request.getAsync());
                response.setOneway(request.getOneway());

                // 设置成功状态
                header.setStatus((byte) RpcStatus.SUCCESS.getCode());
            } catch (Throwable t) {
                // 异常处理：记录错误信息并设置失败状态
                response.setError(t.toString());
                header.setStatus((byte) RpcStatus.FAIL.getCode());
                logger.error("RPC Server handle request error", t);
            }

            // 组装响应协议
            rpcResponseRpcProtocol.setHeader(header);
            rpcResponseRpcProtocol.setBody(response);

            // 将响应写回客户端
            // 1. writeAndFlush会触发编码器将对象转换为字节流
            // 2. 返回ChannelFuture，可以添加监听器处理写操作完成事件
            // 3. 这是异步操作，不会阻塞当前线程
            ctx.writeAndFlush(rpcResponseRpcProtocol).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    logger.debug("Send response for request " + header.getRequestId());
                }
            });
        });
    }

    /**
     * 处理RPC请求的核心业务逻辑
     * 根据请求信息找到对应的服务实现，并通过反射调用目标方法
     *
     * @param request RPC请求对象，包含类名、方法名、参数等信息
     * @return 方法执行结果
     * @throws Throwable 方法执行过程中可能抛出的异常
     */
    private Object handle(RpcRequest request) throws Throwable {
        // 构建服务标识key，格式为：className:version:group
        String serviceKey = RpcServiceHelper.buildServiceKey(request.getClassName(), request.getVersion(), request.getGroup());

        // 从服务映射表中获取服务实现对象
        Object serviceBean = handlerMap.get(serviceKey);
        if (serviceBean == null) {
            throw new RuntimeException(String.format("service not exist: %s:%s", request.getClassName(), request.getMethodName()));
        }

        // 获取服务实现类的Class对象
        Class<?> serviceClass = serviceBean.getClass();

        // 提取请求参数
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        logger.debug(serviceClass.getName());
        logger.debug(methodName);

        // 输出参数类型信息
        if (parameterTypes != null && parameterTypes.length > 0) {
            for (int i = 0; i < parameterTypes.length; ++i) {
                logger.debug(parameterTypes[i].getName());
            }
        }

        // 输出参数值信息
        if (parameters != null && parameters.length > 0) {
            for (int i = 0; i < parameters.length; ++i) {
                logger.debug(parameters[i].toString());
            }
        }

        // 通过反射调用目标方法
        return invokeMethod(serviceBean, serviceClass, methodName, parameterTypes, parameters);
    }

    /**
     * 通过反射调用目标方法
     * TODO 目前使用JDK动态代理方式，此处埋点
     * <p>
     * 扩展点说明：
     * 1. 可以在此处添加方法调用拦截器
     * 2. 可以集成AOP框架进行增强处理
     * 3. 可以添加性能监控、限流等功能
     *
     * @param serviceBean    服务实现对象
     * @param serviceClass   服务实现类
     * @param methodName     方法名
     * @param parameterTypes 参数类型数组
     * @param parameters     参数值数组
     * @return 方法执行结果
     * @throws Throwable 方法执行过程中可能抛出的异常
     */
    private Object invokeMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Throwable {
        Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(serviceBean, parameters);
    }

    /**
     * 异常处理方法
     * 当通道处理过程中发生异常时，此方法会被调用
     * <p>
     * 1. exceptionCaught是ChannelHandler接口的方法
     * 2. 当Pipeline中任何Handler抛出异常时都会触发
     * 3. 通常在最后一个Handler中处理异常并关闭连接
     *
     * @param ctx   通道处理器上下文
     * @param cause 异常原因
     * @throws Exception 处理异常时可能抛出的异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("server caught exception", cause);
        ctx.close();
    }
}
