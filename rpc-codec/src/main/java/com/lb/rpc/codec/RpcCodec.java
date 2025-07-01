package com.lb.rpc.codec;

import com.lb.rpc.serialization.api.Serialization;
import com.lb.rpc.spi.loader.ExtensionLoader;

/**
 * RPC编解码器接口
 * 定义了RPC协议的编码和解码的基础能力
 */
public interface RpcCodec {

    default Serialization getSerialization(String serializationType) {
        return ExtensionLoader.getExtension(Serialization.class, serializationType);
    }

}
