package com.lb.rpc.annotation;

/**
 * RPC服务消费者注解
 * <p>
 * 该注解用于标识一个字段或方法参数作为RPC服务的消费者，被标注的字段将会被注入
 * 对应的RPC服务代理对象，通过该代理对象可以进行远程服务调用。
 * </p>
 * 
 * <p>使用示例：</p>
 * <pre>
 * {@code
 * public class UserController {
 *     @RpcReference(version = "1.0.0", timeout = 3000)
 *     private UserService userService;
 *     
 *     public User getUser(Long id) {
 *         return userService.findById(id);
 *     }
 * }
 * }
 * </pre>
 * 
 * @author lb
 * @since 1.0.0
 */
public @interface RpcReference {

    /**
     * 服务版本号
     * <p>
     * 指定要调用的服务版本，必须与服务提供者的版本号匹配才能正确调用。
     * 支持服务的版本管理和平滑升级。
     * </p>
     * 
     * @return 服务版本号，默认为"1.0.0"
     */
    String version() default "1.0.0";

    /**
     * 注册中心类型
     * <p>
     * 指定服务发现使用的注册中心类型，系统根据该配置连接对应的注册中心
     * 获取服务提供者的地址信息。
     * </p>
     * 
     * <p>支持的注册中心类型：</p>
     * <ul>
     *   <li>zookeeper - Apache ZooKeeper</li>
     *   <li>nacos - 阿里巴巴Nacos</li>
     *   <li>etcd - CoreOS etcd</li>
     *   <li>consul - HashiCorp Consul</li>
     * </ul>
     * 
     * @return 注册中心类型，默认为"zookeeper"
     */
    String registryType() default "zookeeper";

    /**
     * 注册中心地址
     * <p>
     * 注册中心的连接地址，支持单机和集群模式。
     * 集群模式下多个地址用逗号分隔。
     * </p>
     * 
     * <p>地址格式示例：</p>
     * <ul>
     *   <li>单机：127.0.0.1:2181</li>
     *   <li>集群：192.168.1.1:2181,192.168.1.2:2181,192.168.1.3:2181</li>
     * </ul>
     * 
     * @return 注册中心地址，默认为"127.0.0.1:2181"
     */
    String registryAddress() default "127.0.0.1:2181";

    /**
     * 负载均衡策略
     * <p>
     * 当服务提供者有多个实例时，客户端通过负载均衡策略选择具体的服务实例进行调用。
     * 不同的策略适用于不同的业务场景。
     * </p>
     * 
     * <p>支持的负载均衡策略：</p>
     * <ul>
     *   <li>zkconsistenthash - 基于ZooKeeper的一致性Hash</li>
     *   <li>random - 随机选择</li>
     *   <li>roundrobin - 轮询</li>
     *   <li>weightedrandom - 加权随机</li>
     *   <li>weightedround - 加权轮询</li>
     * </ul>
     * 
     * @return 负载均衡类型，默认为"zkconsistenthash"
     */
    String loadBalanceType() default "zkconsistenthash";

    /**
     * 序列化类型
     * <p>
     * 指定RPC调用过程中请求和响应数据的序列化方式。
     * 不同的序列化方式在性能、体积、兼容性等方面各有特点。
     * </p>
     * 
     * <p>支持的序列化类型：</p>
     * <ul>
     *   <li>protostuff - Google Protostuff，高性能</li>
     *   <li>kryo - Kryo序列化，速度快</li>
     *   <li>json - JSON格式，可读性强</li>
     *   <li>jdk - JDK原生序列化，兼容性好</li>
     *   <li>hessian2 - Hessian2序列化，跨语言</li>
     *   <li>fst - Fast-serialization，高性能</li>
     * </ul>
     * 
     * @return 序列化类型，默认为"protostuff"
     */
    String serializationType() default "protostuff";

    /**
     * 调用超时时间
     * <p>
     * 设置RPC调用的超时时间，单位为毫秒。
     * 超过该时间未收到响应将抛出超时异常。
     * 合理设置超时时间可以避免调用方长时间等待。
     * </p>
     * 
     * @return 超时时间（毫秒），默认为5000ms（5秒）
     */
    long timeout() default 5000;

    /**
     * 是否异步执行
     * <p>
     * 设置为true时，RPC调用将以异步方式执行，调用方不会阻塞等待结果。
     * 异步调用适用于不需要立即获取结果的场景，可以提高系统吞吐量。
     * </p>
     * 
     * @return true表示异步执行，false表示同步执行，默认为false
     */
    boolean async() default false;

    /**
     * 是否单向调用
     * <p>
     * 设置为true时，客户端发送请求后不等待服务端响应，适用于日志记录、
     * 消息通知等不需要返回值的场景。单向调用可以减少网络开销。
     * </p>
     * 
     * @return true表示单向调用，false表示双向调用，默认为false
     */
    boolean oneway() default false;

    /**
     * 代理类型
     * <p>
     * 指定客户端生成服务代理对象的方式。不同的代理方式在性能和兼容性上有所差异。
     * </p>
     * 
     * <p>支持的代理类型：</p>
     * <ul>
     *   <li>jdk - JDK动态代理，基于接口</li>
     *   <li>javassist - Javassist字节码代理，性能较好</li>
     *   <li>cglib - CGLIB代理，支持类代理</li>
     * </ul>
     * 
     * @return 代理类型，默认为"jdk"
     */
    String proxy() default "jdk";

    /**
     * 服务分组
     * <p>
     * 指定要调用的服务分组，必须与服务提供者的分组匹配。
     * 通过分组可以实现服务的逻辑隔离和分类管理。
     * </p>
     * 
     * @return 服务分组名称，默认为空字符串表示默认分组
     */
    String group() default "";
}