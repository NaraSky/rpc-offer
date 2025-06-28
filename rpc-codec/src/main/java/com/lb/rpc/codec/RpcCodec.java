package com.lb.rpc.codec;

import com.lb.rpc.serialization.api.Serialization;
import com.lb.rpc.serialization.jdk.JdkSerialization;

/**
 * RPC编解码器接口
 * 定义了RPC协议的编码和解码的基础能力
 */
public interface RpcCodec {

    default Serialization getJdkSerialization(){
        return new JdkSerialization();
    }

}
