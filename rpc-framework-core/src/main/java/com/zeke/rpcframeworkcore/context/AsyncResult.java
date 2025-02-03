package com.zeke.rpcframeworkcore.context;

import com.zeke.rpcframeworkcore.remoting.dto.RpcResponse;

import java.util.concurrent.CompletableFuture;

public class AsyncResult {
    public static ThreadLocal<CompletableFuture<Object>> threadLocal = new ThreadLocal<>();

    public static void setCurrentResult(CompletableFuture<Object> result) {
        threadLocal.set(result);
    }

    public static CompletableFuture<Object> getCurrentResult() {
        return threadLocal.get();
    }

    public static void removeCurrentResult() {
        threadLocal.remove();
    }
}
