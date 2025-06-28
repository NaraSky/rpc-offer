package com.lb.rpc.protocol.header;


import java.io.Serializable;

/**
 * 消息头，目前固定为32个字节
 */
public class RpcHeader implements Serializable {
     /*
    +---------------------------------------------------------------+
    | 魔数 2byte | 报文类型 1byte | 状态 1byte |     消息 ID 8byte      |
    +---------------------------------------------------------------+
    |           序列化类型 16byte      |        数据长度 4byte          |
    +---------------------------------------------------------------+
    */

    // 魔术 2byte
    private short magic;
    // 报文类型 1byte
    private byte msgType;
    // 状态 1byte
    private byte status;
    // 消息ID 8byte
    private long requestId;
    // 序列化类型 16byte，不足16字节后面补0，约定序列化类型长度最多不能超过16
    private String serializationType;
    // 消息长度 4byte
    private int msgLen;

    public short getMagic() {
        return magic;
    }

    public void setMagic(short magic) {
        this.magic = magic;
    }

    public byte getMsgType() {
        return msgType;
    }

    public void setMsgType(byte msgType) {
        this.msgType = msgType;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public String getSerializationType() {
        return serializationType;
    }

    public void setSerializationType(String serializationType) {
        this.serializationType = serializationType;
    }

    public int getMsgLen() {
        return msgLen;
    }

    public void setMsgLen(int msgLen) {
        this.msgLen = msgLen;
    }
}
