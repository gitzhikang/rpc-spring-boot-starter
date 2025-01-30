package com.zeke.testclient.service;

import com.zeke.rpcframeworkcore.annotation.RpcReference;
import org.springframework.stereotype.Service;
import com.zeke.testapi.IUserService;

@Service
public class UserBusiness {

    @RpcReference(version = "version1", group = "test1")
    IUserService userService;

    public String sayHello(String name) {
        return userService.sayHello(name);
    }
}
