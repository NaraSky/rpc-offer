package com.lb.rpc.serialization.api;

import com.lb.rpc.constants.RpcConstants;
import com.lb.rpc.spi.annotation.SPI;

@SPI(RpcConstants.SERIALIZATION_JDK)
public interface Serialization {

    /**
     * 序列化
     */
    <T> byte[] serialize(T obj);

    /**
     * 反序列化
     */
    <T> T deserialize(byte[] data, Class<T> cls);
}
