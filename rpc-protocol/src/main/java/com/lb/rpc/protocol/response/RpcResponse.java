package com.lb.rpc.protocol.response;

import com.lb.rpc.protocol.base.RpcMessage;

public class RpcResponse extends RpcMessage {

    private static final long serialVersionUID = 6786887933307915450L;

    private String error;
    private Object result;

    public boolean isError() {
        return error != null;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
