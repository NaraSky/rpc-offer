package com.lb.rpc.proxy.api.callback;

public interface AsyncRPCCallback {

    /**
     * 成功后的回调方法
     */
    void onSuccess(Object result);

    /**
     * 异常的回调方法
     */
    void onException(Exception e);
}
