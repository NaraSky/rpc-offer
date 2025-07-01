package com.lb.rpc.spi.annotation;

import java.lang.annotation.*;

/**
 * SPI实现类注解
 * 用于标记SPI接口的具体实现类
 *
 * 所有SPI接口的实现类都必须使用此注解标记
 * 这样ExtensionLoader才能正确识别和加载这些实现类
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SPIClass {
}
