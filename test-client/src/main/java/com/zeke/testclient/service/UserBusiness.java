package com.zeke.testclient.service;

import com.zeke.IUserService;
import com.zeke.rpcframeworkcore.annotation.RpcReference;
import com.zeke.rpcframeworkcore.context.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class UserBusiness {

    @RpcReference(version = "version1", group = "test1",async = true)
    IUserService userService;


    public String sayHello(String name) throws ExecutionException, InterruptedException {
        userService.sayHello(name);
        CompletableFuture<Object> currentResult = AsyncResult.getCurrentResult();
        return (String) currentResult.get();
    }
}
