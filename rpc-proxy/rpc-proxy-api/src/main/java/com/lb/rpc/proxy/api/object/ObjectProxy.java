package com.lb.rpc.proxy.api.object;

import com.lb.rpc.protocol.RpcProtocol;
import com.lb.rpc.protocol.header.RpcHeaderFactory;
import com.lb.rpc.protocol.request.RpcRequest;
import com.lb.rpc.proxy.api.consumer.Consumer;
import com.lb.rpc.proxy.api.future.RPCFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public class ObjectProxy<T> implements InvocationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectProxy.class);

    /**
     * 接口的Class对象
     */
    private Class<T> clazz;

    /**
     * 服务版本号
     */
    private String serviceVersion;
    /**
     * 服务分组
     */
    private String serviceGroup;
    /**
     * 超时时间，默认15s
     */
    private long timeout = 15000;
    /**
     * 服务消费者
     */
    private Consumer consumer;
    /**
     * 序列化类型
     */
    private String serializationType;

    /**
     * 是否异步调用
     */
    private boolean async;

    /**
     * 是否单向调用
     */
    private boolean oneway;

    public ObjectProxy(Class<T> clazz) {
        this.clazz = clazz;
    }

    public ObjectProxy(Class<T> clazz, String serviceVersion, String serviceGroup, String serializationType, long timeout, Consumer consumer, boolean async, boolean oneway) {
        this.clazz = clazz;
        this.serviceVersion = serviceVersion;
        this.timeout = timeout;
        this.serviceGroup = serviceGroup;
        this.consumer = consumer;
        this.serializationType = serializationType;
        this.async = async;
        this.oneway = oneway;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 1. 处理Object类方法
        if (Object.class.equals(method.getDeclaringClass())) {
            // 特殊处理equals、hashCode、toString
            String name = method.getName();
            if ("equals".equals(name)) {
                return proxy == args[0];
            } else if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            } else if ("toString".equals(name)) {
                return proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy)) + ", with InvocationHandler " + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }

        // 2. 构建RPC请求
        RpcProtocol<RpcRequest> requestRpcProtocol = new RpcProtocol<>();
        // 设置协议头和请求体..
        requestRpcProtocol.setHeader(RpcHeaderFactory.getRequestHeader(serializationType));

        RpcRequest request = new RpcRequest();
        request.setVersion(this.serviceVersion);
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setGroup(this.serviceGroup);
        request.setParameters(args);
        request.setAsync(async);
        request.setOneway(oneway);
        requestRpcProtocol.setBody(request);

        LOGGER.debug(method.getDeclaringClass().getName());
        LOGGER.debug(method.getName());

        if (method.getParameterTypes() != null && method.getParameterTypes().length > 0) {
            for (int i = 0; i < method.getParameterTypes().length; ++i) {
                LOGGER.debug(method.getParameterTypes()[i].getName());
            }
        }

        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; ++i) {
                LOGGER.debug(args[i].toString());
            }
        }

        // 3. 发送请求并获取Future
        RPCFuture rpcFuture = this.consumer.sendRequest(requestRpcProtocol);

        // 4. 根据超时设置返回结果
        return rpcFuture == null ? null :
                timeout > 0 ? rpcFuture.get(timeout, TimeUnit.MILLISECONDS) : rpcFuture.get();
    }
}
