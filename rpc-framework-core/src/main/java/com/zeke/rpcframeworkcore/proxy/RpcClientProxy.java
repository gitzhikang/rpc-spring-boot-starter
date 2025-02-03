package com.zeke.rpcframeworkcore.proxy;


import com.zeke.rpcframeworkcommon.enums.RpcErrorMessageEnum;
import com.zeke.rpcframeworkcommon.enums.RpcResponseCodeEnum;
import com.zeke.rpcframeworkcommon.exception.RpcException;
import com.zeke.rpcframeworkcore.config.RpcServiceConfig;
import com.zeke.rpcframeworkcore.context.AsyncResult;
import com.zeke.rpcframeworkcore.remoting.dto.RpcRequest;
import com.zeke.rpcframeworkcore.remoting.dto.RpcResponse;
import com.zeke.rpcframeworkcore.remoting.transport.RpcRequestTransport;
import com.zeke.rpcframeworkcore.remoting.transport.netty.client.NettyRpcClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Dynamic proxy class.
 * When a dynamic proxy object calls a method, it actually calls the following invoke method.
 * It is precisely because of the dynamic proxy that the remote method called by the client is like calling the local method (the intermediate process is shielded)
 *
 * @author shuang.kou
 * @createTime 2020年05月10日 19:01:00
 */
@Slf4j
public class RpcClientProxy implements InvocationHandler {

    private static final String INTERFACE_NAME = "interfaceName";

    /**
     * Used to send requests to the server.And there are two implementations: socket and netty
     */
    private final RpcRequestTransport rpcRequestTransport;
    private final RpcServiceConfig rpcServiceConfig;

    public RpcClientProxy(RpcRequestTransport rpcRequestTransport, RpcServiceConfig rpcServiceConfig) {
        this.rpcRequestTransport = rpcRequestTransport;
        this.rpcServiceConfig = rpcServiceConfig;
    }


    public RpcClientProxy(RpcRequestTransport rpcRequestTransport) {
        this.rpcRequestTransport = rpcRequestTransport;
        this.rpcServiceConfig = new RpcServiceConfig();
    }

    /**
     * get the proxy object
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    /**
     * This method is actually called when you use a proxy object to call a method.
     * The proxy object is the object you get through the getProxy method.
     */
    @SneakyThrows
    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        log.info("invoked method: [{}]", method.getName());
        String methodName = method.getName().endsWith("Async") && rpcServiceConfig.isAsync()? method.getName().substring(0, method.getName().length() - 5) : method.getName();
        RpcRequest rpcRequest = RpcRequest.builder().methodName(methodName)
                .parameters(args)
                .interfaceName(method.getDeclaringClass().getName())
                .paramTypes(method.getParameterTypes())
                .requestId(UUID.randomUUID().toString())
                .group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion())
                .build();
        if (rpcRequestTransport instanceof NettyRpcClient) {
            CompletableFuture<RpcResponse<Object>> completableFuture = (CompletableFuture<RpcResponse<Object>>) rpcRequestTransport.sendRpcRequest(rpcRequest);
            if (rpcServiceConfig.isAsync()) {
                // 如果返回值类型是 CompletableFuture，直接返回
                CompletableFuture<Object> result = completableFuture.thenApply(rpcResponse -> {
                    this.check(rpcResponse, rpcRequest);
                    return rpcResponse.getData();
                });
                AsyncResult.setCurrentResult(result);
                return null;
            } else {
                // 如果返回值类型是普通类型，阻塞等待结果
                RpcResponse<Object> rpcResponse = completableFuture.get(); // 这里会阻塞
                this.check(rpcResponse, rpcRequest);
                return rpcResponse.getData();
            }
        }
//        if (rpcRequestTransport instanceof SocketRpcClient) {
//            rpcResponse = (RpcResponse<Object>) rpcRequestTransport.sendRpcRequest(rpcRequest);
//        }
        return null;
    }

    private void check(RpcResponse<Object> rpcResponse, RpcRequest rpcRequest) {
        if (rpcResponse == null) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }

        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
            throw new RpcException(RpcErrorMessageEnum.REQUEST_NOT_MATCH_RESPONSE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }

        if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCodeEnum.SUCCESS.getCode())) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
    }
}
