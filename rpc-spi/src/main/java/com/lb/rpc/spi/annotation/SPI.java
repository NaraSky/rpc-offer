package com.lb.rpc.spi.annotation;

import java.lang.annotation.*;

/**
 * SPI（Service Provider Interface）注解
 * 用于标记接口，表示该接口支持SPI扩展机制
 * <p>
 * SPI是一种服务发现机制，允许第三方为接口提供实现
 * 类似于Java原生的ServiceLoader，但功能更强大
 */
@Documented     // 该注解会被javadoc工具记录
@Retention(RetentionPolicy.RUNTIME) // 注解在运行时保留，可通过反射获取
@Target({ElementType.TYPE})     // 只能用于类、接口、枚举
public @interface SPI {

    /**
     * 默认的实现方式名称
     * 当没有指定具体实现时，会使用这个默认值
     *
     * @return 默认实现的名称
     */
    String value() default "";
}
