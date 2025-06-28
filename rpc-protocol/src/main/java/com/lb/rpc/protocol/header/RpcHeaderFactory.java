package com.lb.rpc.protocol.header;

import com.lb.rpc.common.id.IdFactory;
import com.lb.rpc.constants.RpcConstants;
import com.lb.rpc.protocol.enumeration.RpcType;

public class RpcHeaderFactory {

    public static RpcHeader getRequestHeader(String serializationType) {
        RpcHeader header = new RpcHeader();
        Long requestId = IdFactory.getId();
        header.setMagic(RpcConstants.MAGIC);
        header.setRequestId(requestId);
        header.setMsgType((byte) RpcType.REQUEST.getType());
        header.setStatus((byte) 0x1);
        header.setSerializationType(serializationType);
        return header;
    }
}
