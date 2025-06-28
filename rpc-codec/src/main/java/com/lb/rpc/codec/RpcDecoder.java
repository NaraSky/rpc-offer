package com.lb.rpc.codec;

import com.lb.rpc.common.utils.SerializationUtils;
import com.lb.rpc.constants.RpcConstants;
import com.lb.rpc.protocol.RpcProtocol;
import com.lb.rpc.protocol.enumeration.RpcType;
import com.lb.rpc.protocol.header.RpcHeader;
import com.lb.rpc.protocol.request.RpcRequest;
import com.lb.rpc.protocol.response.RpcResponse;
import com.lb.rpc.serialization.api.Serialization;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;

import java.util.List;

/**
 * RPC解码器
 * 继承Netty的ByteToMessageDecoder，负责将字节流解码为RpcProtocol对象
 */
public class RpcDecoder extends ByteToMessageDecoder implements RpcCodec {
    /**
     * 解码方法，将字节流转换为RPC协议对象
     *
     * @param ctx Netty通道处理器上下文
     * @param in  输入的字节缓冲区
     * @param out 输出的对象列表，解码成功的对象会添加到此列表
     * @throws Exception 解码过程中可能抛出的异常
     */
    @Override
    public final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 检查可读字节数是否足够读取完整的消息头
        if (in.readableBytes() < RpcConstants.HEADER_TOTAL_LEN) {
            return; // 数据不完整，等待更多数据
        }
        // 标记当前读取位置，用于回退
        in.markReaderIndex();

        // 读取魔数（2字节）
        short magic = in.readShort();
        // 验证魔数是否正确
        if (magic != RpcConstants.MAGIC) {
            throw new IllegalArgumentException("magic number is illegal, " + magic);
        }

        // 读取消息类型（1字节）
        byte msgType = in.readByte();
        // 读取状态码（1字节）
        byte status = in.readByte();
        // 读取请求ID（8字节）
        long requestId = in.readLong();

        // 读取序列化类型字符串（固定长度）
        ByteBuf serializationTypeByteBuf = in.readBytes(SerializationUtils.MAX_SERIALIZATION_TYPE_LENGTH);
        String serializationType = SerializationUtils.subString(serializationTypeByteBuf.toString(CharsetUtil.UTF_8));

        // 读取数据长度（4字节）
        int dataLength = in.readInt();
        // 检查剩余可读字节数是否足够读取完整的数据部分
        if (in.readableBytes() < dataLength) {
            // 数据不完整，重置读取位置，等待更多数据
            in.resetReaderIndex();
            return;
        }
        // 读取序列化后的数据
        byte[] data = new byte[dataLength];
        in.readBytes(data);

        // 根据消息类型获取对应的枚举
        RpcType msgTypeEnum = RpcType.findByType(msgType);
        if (msgTypeEnum == null) {
            return; // 未知消息类型，忽略
        }

        // 构建RPC消息头对象
        RpcHeader header = new RpcHeader();
        header.setMagic(magic);
        header.setStatus(status);
        header.setRequestId(requestId);
        header.setMsgType(msgType);
        header.setSerializationType(serializationType);
        header.setMsgLen(dataLength);
        //TODO Serialization是扩展点 - 未来可支持多种序列化方式
        Serialization serialization = getJdkSerialization();

        // 根据消息类型进行不同的处理
        switch (msgTypeEnum) {
            case REQUEST:
                // 处理RPC请求消息
                RpcRequest request = serialization.deserialize(data, RpcRequest.class);
                if (request != null) {
                    // 构建请求协议对象
                    RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
                    protocol.setHeader(header);
                    protocol.setBody(request);
                    out.add(protocol);   // 添加到输出列表
                }
                break;
            case RESPONSE:
                // 处理RPC响应消息
                RpcResponse response = serialization.deserialize(data, RpcResponse.class);
                if (response != null) {
                    // 构建响应协议对象
                    RpcProtocol<RpcResponse> protocol = new RpcProtocol<>();
                    protocol.setHeader(header);
                    protocol.setBody(response);
                    out.add(protocol);  // 添加到输出列表
                }
                break;
            case HEARTBEAT:
                // TODO 处理心跳消息
                break;
        }
    }
}
