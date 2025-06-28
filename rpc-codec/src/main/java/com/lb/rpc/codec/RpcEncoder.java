package com.lb.rpc.codec;

import com.lb.rpc.common.utils.SerializationUtils;
import com.lb.rpc.protocol.RpcProtocol;
import com.lb.rpc.protocol.header.RpcHeader;
import com.lb.rpc.serialization.api.Serialization;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * RPC编码器
 * 继承Netty的MessageToByteEncoder，负责将RpcProtocol对象编码为字节流
 */
public class RpcEncoder extends MessageToByteEncoder<RpcProtocol<Object>> implements RpcCodec {

    /**
     * 编码方法，将RPC协议对象转换为字节流
     *
     * @param ctx     Netty通道处理器上下文
     * @param msg     待编码的RPC协议对象
     * @param byteBuf 输出的字节缓冲区
     * @throws Exception 编码过程中可能抛出的异常
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcProtocol<Object> msg, ByteBuf byteBuf) throws Exception {
        // 获取消息头
        RpcHeader header = msg.getHeader();
        // 写入魔数(2byte)  -用于识别协议
        byteBuf.writeShort(header.getMagic());
        // 写入消息类型（1字节）- 请求/响应/心跳等
        byteBuf.writeByte(header.getMsgType());
        // 写入状态码（1字节）- 成功/失败等状态
        byteBuf.writeByte(header.getStatus());
        // 写入请求ID（8字节）- 用于请求响应匹配
        byteBuf.writeLong(header.getRequestId());
        // 获取序列化类型
        String serializationType = header.getSerializationType();
        //TODO Serialization是扩展点 - 未来可支持多种序列化方式
        Serialization serialization = getJdkSerialization();
        // 写入序列化类型字符串（固定长度，UTF-8编码）
        byteBuf.writeBytes(SerializationUtils.paddingString(serializationType).getBytes("UTF-8"));
        // 序列化消息体
        byte[] data = serialization.serialize(msg.getBody());
        // 写入数据长度（4字节）
        byteBuf.writeInt(data.length);
        // 写入序列化后的数据
        byteBuf.writeBytes(data);
    }
}
