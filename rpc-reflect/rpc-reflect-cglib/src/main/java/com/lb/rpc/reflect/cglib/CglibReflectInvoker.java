package com.lb.rpc.reflect.cglib;

import com.lb.rpc.reflect.api.ReflectInvoker;
import com.lb.rpc.spi.annotation.SPIClass;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CGLib反射实现
 * <p>
 * 【技术原理】
 * CGLib通过字节码生成技术创建FastClass和FastMethod，
 * 避免了JDK反射的性能开销，调用速度接近直接方法调用。
 * <p>
 * 【性能特征】
 * - 优点：调用性能优秀，比JDK反射快很多
 * - 缺点：首次调用时需要生成字节码，有初始化开销
 * - 适用场景：高频调用，对性能要求较高的场景
 */
@SPIClass
public class CglibReflectInvoker implements ReflectInvoker {
    private final Logger logger = LoggerFactory.getLogger(CglibReflectInvoker.class);

    /**
     * 使用CGLIB的FastClass和FastMethod调用方法
     */
    @Override
    public Object invokeMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Throwable {
        logger.info("use cglib reflect type invoke method...");
        // 【FastClass创建】CGLib为目标类创建一个FastClass
        // FastClass通过索引访问方法，避免了反射的开销
        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
        return serviceFastMethod.invoke(serviceBean, parameters);
    }
}
