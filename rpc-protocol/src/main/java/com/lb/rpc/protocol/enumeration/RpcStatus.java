package com.lb.rpc.protocol.enumeration;

/**
 * RPC服务状态
 */
public enum RpcStatus {

    SUCCESS(0),
    FAIL(1);

    private final int code;

    RpcStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
