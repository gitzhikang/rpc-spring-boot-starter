package com.zeke.testserver;

import com.zeke.rpcframeworkcore.annotation.RpcService;
import com.zeke.testapi.IUserService;

@RpcService(group = "test1", version = "version1")
public class UserServiceImpl implements IUserService {

    public String sayHello(String name) {
        return "Hello "+ name;
    }
}
