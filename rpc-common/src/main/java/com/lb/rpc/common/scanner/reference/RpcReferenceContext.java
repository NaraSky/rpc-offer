package com.lb.rpc.common.scanner.reference;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPC服务引用代理实例上下文管理器
 * <p>
 * 该类用于全局管理标注了@RpcReference注解的字段对应的代理实例，
 * 作为一个单例的缓存容器，存储RPC客户端代理对象，供应用程序使用。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>缓存RPC服务的代理实例对象</li>
 *   <li>提供线程安全的实例存取操作</li>
 *   <li>避免重复创建相同的代理对象</li>
 *   <li>统一管理所有RPC客户端代理实例</li>
 * </ul>
 *
 * @author lb
 * @since 1.0.0
 */
public class RpcReferenceContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcReferenceContext.class);

    /**
     * 代理实例缓存容器
     * <p>
     * 使用ConcurrentHashMap确保多线程环境下的数据安全性。
     * Key：通常是服务接口的完全限定名或唯一标识
     * Value：对应的RPC代理实例对象
     * </p>
     *
     * <p>设计考虑：</p>
     * <ul>
     *   <li>volatile关键字保证可见性</li>
     *   <li>ConcurrentHashMap保证线程安全</li>
     *   <li>静态变量确保全局唯一</li>
     * </ul>
     */
    private static volatile Map<String, Object> instance;

    static {
        instance = new ConcurrentHashMap<>();
    }

    public static void put(String key, Object value) {
        LOGGER.debug("放入代理对象: key={}, value={}", key, value);
        instance.put(key, value);
    }

    public static Object get(String key) {
        Object value = instance.get(key);
        LOGGER.debug("获取代理对象: key={}, value={}", key, value);
        return value;
    }

    public static Object remove(String key) {
        Object value = instance.remove(key);
        LOGGER.debug("移除代理对象: key={}, value={}", key, value);
        return value;
    }
}