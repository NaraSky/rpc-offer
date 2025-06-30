package com.lb.rpc.consumer.common.context;

import com.lb.rpc.consumer.common.future.RPCFuture;

public class RpcContext {
    public RpcContext() {
    }

    private static final RpcContext AGENT = new RpcContext();

    private static final InheritableThreadLocal<RPCFuture> RPC_FUTURE_THREAD_LOCAL = new InheritableThreadLocal<>();

    public static RpcContext getContext() {
        return AGENT;
    }

    public void setRpcFuture(RPCFuture rpcFuture) {
        RPC_FUTURE_THREAD_LOCAL.set(rpcFuture);
    }

    public RPCFuture getRpcFuture(){
        return RPC_FUTURE_THREAD_LOCAL.get();
    }

    public void removeRpcFuture(){
        RPC_FUTURE_THREAD_LOCAL.remove();
    }
}
