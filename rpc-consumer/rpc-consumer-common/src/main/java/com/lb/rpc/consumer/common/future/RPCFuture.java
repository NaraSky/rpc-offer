package com.lb.rpc.consumer.common.future;

import com.lb.rpc.protocol.RpcProtocol;
import com.lb.rpc.protocol.request.RpcRequest;
import com.lb.rpc.protocol.response.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class RPCFuture extends CompletableFuture<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RPCFuture.class);

    private Sync sync;                              // 自定义同步器，控制线程等待和唤醒
    private RpcProtocol<RpcRequest> requestRpcProtocol;   // 请求协议对象
    private RpcProtocol<RpcResponse> responseRpcProtocol; // 响应协议对象
    private long startTime;                         // 请求开始时间
    private long responseTimeThreshold = 5000;      // 响应时间阈值（5秒）

    public RPCFuture(RpcProtocol<RpcRequest> requestRpcProtocol) {
        this.sync = new Sync();
        this.requestRpcProtocol = requestRpcProtocol;
        this.startTime = System.currentTimeMillis();     // 记录请求开始时间
    }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        sync.acquire(-1);   // 调用sync.acquire(-1)阻塞当前线程
        if (this.responseRpcProtocol != null) {
            return this.responseRpcProtocol.getBody().getResult();
        }
        return null;
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean success = sync.tryAcquireNanos(-1, unit.toNanos(timeout));
        if (success) {
            // 在超时时间内获得响应
            if (this.responseRpcProtocol != null) {
                return this.responseRpcProtocol.getBody().getResult();
            } else {
                return null;
            }
        } else {
            // 超时，抛出异常
            throw new RuntimeException("Timeout exception. Request id: " + this.requestRpcProtocol.getHeader().getRequestId()
                    + ". Request class name: " + this.requestRpcProtocol.getBody().getClassName()
                    + ". Request method: " + this.requestRpcProtocol.getBody().getMethodName());
        }
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    public void done(RpcProtocol<RpcResponse> responseRpcProtocol) {
        this.responseRpcProtocol = responseRpcProtocol;
        sync.release(1);  // 释放锁，唤醒等待的线程

        // 性能监控：检查响应时间
        long responseTime = System.currentTimeMillis() - startTime;
        if (responseTime > this.responseTimeThreshold) {
            LOGGER.warn("Service response time is too slow. Request id = " +
                    responseRpcProtocol.getHeader().getRequestId() +
                    ". Response Time = " + responseTime + "ms");
        }
    }

    static class Sync extends AbstractQueuedSynchronizer {
        private final int done = 1;      // 已完成状态
        private final int pending = 0;   // 等待状态

        // 尝试获取锁，只有状态为done时才能获取成功
        @Override
        protected boolean tryAcquire(int arg) {
            return getState() == done;
        }

        // 释放锁，将状态从pending改为done
        @Override
        protected boolean tryRelease(int arg) {
            // 原子性地修改状态，保证线程安全
            if (compareAndSetState(pending, done)) {
                return true;
            }
            return false;
        }

        public boolean isDone() {
            return getState() == done;
        }
    }

}
