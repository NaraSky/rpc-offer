package com.lb.rpc.provider.common.handler;

import com.lb.rpc.common.helper.RpcServiceHelper;
import com.lb.rpc.common.threadpool.ServerThreadPool;
import com.lb.rpc.protocol.RpcProtocol;
import com.lb.rpc.protocol.enumeration.RpcStatus;
import com.lb.rpc.protocol.enumeration.RpcType;
import com.lb.rpc.protocol.header.RpcHeader;
import com.lb.rpc.protocol.request.RpcRequest;
import com.lb.rpc.protocol.response.RpcResponse;
import com.lb.rpc.reflect.api.ReflectInvoker;
import com.lb.rpc.spi.loader.ExtensionLoader;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * RPC服务提供者的核心Handler处理类
 * <p>
 * 【设计模式思考】
 * 这个类继承自SimpleChannelInboundHandler，这是Netty提供的一个特殊的入站处理器。
 * 与普通的ChannelInboundHandlerAdapter不同，SimpleChannelInboundHandler有几个重要特性：
 * 1. 自动类型匹配：只处理指定泛型类型的消息（这里是RpcProtocol<RpcRequest>）
 * 2. 自动资源释放：处理完消息后自动调用ReferenceCountUtil.release()释放ByteBuf等资源
 * 3. 简化异常处理：提供更清晰的异常处理机制
 * <p>
 * 【线程模型分析】
 * Netty的EventLoop是单线程的，如果在channelRead0中执行耗时操作会阻塞整个EventLoop，
 * 影响其他连接的处理。因此这里采用线程池异步处理业务逻辑的方式。
 */
public class RpcProviderHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {
    private final Logger logger = LoggerFactory.getLogger(RpcProviderHandler.class);

    /**
     * 服务处理器映射表 - RPC服务注册中心
     * <p>
     * 【数据结构设计】
     * key格式: className:version:group (例如: com.example.UserService:1.0.0:default)
     * value: 服务的具体实现对象实例
     * <p>
     * 这种设计支持：
     * - 同一个服务的多版本共存
     * - 服务分组管理
     * - 快速服务定位（O(1)时间复杂度）
     */
    private final Map<String, Object> handlerMap;

    /**
     * 反射调用器 - SPI扩展点
     * <p>
     * 【SPI机制应用】
     * 通过SPI机制支持多种反射实现：JDK原生反射、CGLib、Javassist、ASM、ByteBuddy等
     * 不同的反射实现在性能上有差异：
     * - JDK反射：兼容性最好，性能一般
     * - CGLib：性能较好，但有类加载开销
     * - Javassist：字节码操作灵活
     * - ASM：性能最好，但使用复杂
     */
    private ReflectInvoker reflectInvoker;

    public RpcProviderHandler(String reflectType, Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
        // 通过SPI扩展加载器获取指定类型的反射调用器实现
        // 这里体现了依赖倒置原则：依赖抽象而不是具体实现
        this.reflectInvoker = ExtensionLoader.getExtension(ReflectInvoker.class, reflectType);
    }

    /**
     * 处理入站RPC请求的核心方法
     * <p>
     * 【方法执行流程】
     * 1. 接收客户端发送的RPC请求协议对象
     * 2. 提交到线程池异步处理（避免阻塞EventLoop）
     * 3. 解析请求信息，定位目标服务
     * 4. 通过反射调用目标方法
     * 5. 构造响应对象并写回客户端
     * <p>
     * 【线程安全考虑】
     * channelRead0方法在Netty的EventLoop线程中执行，该线程是单线程的，
     * 因此对于每个连接的处理是线程安全的。但业务逻辑提交到线程池后，
     * 需要考虑共享资源的线程安全问题。
     *
     * @param ctx      通道处理器上下文 - 提供读写数据、管理通道状态的能力
     * @param protocol 接收到的RPC协议对象 - 包含请求头(header)和请求体(body)
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) throws Exception {
        System.out.println("【服务端】收到RPC请求：" + protocol);
        logger.info("收到RPC请求: {}", protocol);
        // 将请求处理逻辑提交到线程池，避免阻塞Netty的EventLoop线程
        // 这是Netty最佳实践：耗时操作应该异步执行，保持EventLoop的高效运转
        ServerThreadPool.submit(() -> {
            // 获取请求头，并设置响应类型
            RpcHeader header = protocol.getHeader();
            // 将消息类型从REQUEST改为RESPONSE，复用同一个header对象
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
     * <p>
     * 【服务定位机制】
     * 1. 根据className、version、group构建服务标识
     * 2. 从handlerMap中查找对应的服务实现
     * 3. 通过反射机制调用目标方法
     * <p>
     * 【反射调用流程】
     * 1. 获取服务实现对象和其Class对象
     * 2. 提取方法名、参数类型、参数值
     * 3. 委托给SPI反射调用器执行实际调用
     *
     * @param request RPC请求对象，包含调用所需的所有信息
     * @return 方法执行结果
     * @throws Throwable 方法执行过程中的任何异常
     */
    private Object handle(RpcRequest request) throws Throwable {
        String serviceKey = RpcServiceHelper.buildServiceKey(request.getClassName(), request.getVersion(), request.getGroup());
        logger.info("handlerMap keys: {}", handlerMap.keySet());
        logger.info("请求key: {}", serviceKey);
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

        // 【反射调用】委托给SPI反射调用器执行
        // 这里使用了策略模式，不同的反射实现有不同的性能特征
        return this.reflectInvoker.invokeMethod(
                serviceBean,      // 目标对象实例
                serviceClass,     // 目标类的Class对象
                methodName,       // 方法名
                parameterTypes,   // 参数类型数组
                parameters        // 参数值数组
        );
    }

    /**
     * 异常处理方法 - Netty异常处理机制
     * <p>
     * 【异常处理策略】
     * 当Pipeline中的任何Handler发生异常时，异常会沿着Pipeline传播，
     * 最终由exceptionCaught方法处理。这里采用简单的策略：
     * 1. 记录异常日志
     * 2. 关闭连接
     * <p>
     * 【生产环境建议】
     * 可以根据异常类型采用不同的处理策略：
     * - 业务异常：返回错误响应，保持连接
     * - 系统异常：记录详细日志，关闭连接
     * - 网络异常：尝试重连或降级处理
     *
     * @param ctx   通道处理器上下文
     * @param cause 异常原因
     * @throws Exception 处理异常时可能抛出的异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("server caught exception", cause);
        // 关闭连接，释放资源
        //todo 这里可以考虑更精细的异常处理策略
        ctx.close();
    }
}
