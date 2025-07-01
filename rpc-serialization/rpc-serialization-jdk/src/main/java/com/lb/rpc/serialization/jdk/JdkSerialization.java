package com.lb.rpc.serialization.jdk;

import com.lb.rpc.common.exception.SerializerException;
import com.lb.rpc.serialization.api.Serialization;
import com.lb.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * JDK序列化实现类
 * 基于Java原生序列化机制实现对象的序列化和反序列化
 * 注意：被序列化的对象必须实现Serializable接口
 */
@SPIClass
public class JdkSerialization implements Serialization {

    private final Logger logger = LoggerFactory.getLogger(JdkSerialization.class);

    /**
     * 序列化方法：将对象转换为字节数组
     *
     * @param obj 待序列化的对象
     * @param <T> 对象类型泛型
     * @return 序列化后的字节数组
     * @throws SerializerException 序列化失败时抛出异常
     */
    @Override
    public <T> byte[] serialize(T obj) {
        logger.info("execute jdk serialize...");
        // 空对象检查：确保传入的对象不为null
        if (obj == null) {
            throw new SerializerException("serialize object is null");
        }
        try {
            // 创建字节数组输出流，用于存储序列化后的字节数据
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            // 创建对象输出流，包装字节数组输出流
            ObjectOutputStream out = new ObjectOutputStream(os);

            // 将对象写入输出流，执行序列化操作
            out.writeObject(obj);

            // 返回序列化后的字节数组
            return os.toByteArray();
        } catch (IOException e) {
            // 捕获IO异常，包装为自定义的序列化异常并抛出
            throw new SerializerException(e.getMessage(), e);
        }
    }

    /**
     * 反序列化方法：将字节数组转换为指定类型的对象
     *
     * @param data 待反序列化的字节数组
     * @param cls  目标对象的Class类型
     * @param <T>  目标对象类型泛型
     * @return 反序列化后的对象实例
     * @throws SerializerException 反序列化失败时抛出异常
     */
    @Override
    public <T> T deserialize(byte[] data, Class<T> cls) {
        logger.info("execute jdk deserialize...");
        // 空数据检查：确保传入的字节数组不为null
        if (data == null) {
            throw new SerializerException("deserialize data is null");
        }
        try {
            // 创建字节数组输入流，用于读取序列化的字节数据
            ByteArrayInputStream is = new ByteArrayInputStream(data);

            // 创建对象输入流，包装字节数组输入流
            ObjectInputStream in = new ObjectInputStream(is);

            // 从输入流中读取对象，执行反序列化操作
            // 使用强制类型转换将Object转换为目标类型T
            return (T) in.readObject();
        } catch (Exception e) {
            // 捕获所有异常（包括IOException、ClassNotFoundException等）
            // 包装为自定义的序列化异常并抛出
            throw new SerializerException(e.getMessage(), e);
        }
    }
}
