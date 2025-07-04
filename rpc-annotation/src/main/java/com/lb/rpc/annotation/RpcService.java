package com.lb.rpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RPC服务提供者注解
 * <p>
 * 该注解用于标识一个类作为RPC服务的提供者，被标注的类将会被注册到服务注册中心，
 * 供RPC客户端进行远程调用。注解继承了Spring的@Component注解，因此被标注的类
 * 会被Spring容器管理。
 * </p>
 * 
 * <p>使用示例：</p>
 * <pre>
 * {@code
 * @RpcService(interfaceClass = UserService.class, version = "1.0.0", group = "user")
 * public class UserServiceImpl implements UserService {
 *     // 服务实现逻辑
 * }
 * }
 * </pre>
 * 
 * @author lb
 * @since 1.0.0
 */
@Target({ElementType.TYPE})  // 只能作用于类上
@Retention(RetentionPolicy.RUNTIME)  // 运行时保留，供反射使用
@Component  // 继承Spring组件注解，使类被Spring容器管理
public @interface RpcService {

    /**
     * 服务接口的Class对象
     * <p>
     * 指定当前服务实现类对应的接口Class，用于服务注册和客户端调用时的类型匹配。
     * 如果不指定，系统会自动推断实现的接口。
     * </p>
     * 
     * @return 接口的Class对象，默认为void.class表示自动推断
     */
    Class<?> interfaceClass() default void.class;

    /**
     * 服务接口的完全限定类名
     * <p>
     * 当无法直接获取接口Class对象时，可以通过字符串形式指定接口的完全限定名。
     * 该属性与interfaceClass()二选一使用，优先使用interfaceClass()。
     * </p>
     * 
     * @return 接口的完全限定类名，默认为空字符串
     */
    String interfaceClassName() default "";

    /**
     * 服务版本号
     * <p>
     * 用于区分同一服务接口的不同版本实现，支持服务的平滑升级和版本管理。
     * 客户端调用时需要指定对应的版本号才能正确调用到对应版本的服务。
     * </p>
     * 
     * @return 服务版本号，默认为"1.0.0"
     */
    String version() default "1.0.0";

    /**
     * 服务分组
     * <p>
     * 用于对服务进行逻辑分组，同一个接口可以有多个不同分组的实现。
     * 在微服务架构中，可以根据业务模块、环境等维度进行分组管理。
     * </p>
     * 
     * @return 服务分组名称，默认为空字符串表示默认分组
     */
    String group() default "";

    /**
     * 权重
     */
    int weight() default 0;

}