package com.zeke.testclient;

import com.zeke.testclient.service.UserBusiness;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.apache.zookeeper.*;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController

public class UserController {

    @Autowired
    private UserBusiness userBusiness;


    @RequestMapping("/sayHello/{name}")
    public String sayHello(@PathVariable String name) throws ExecutionException, InterruptedException {
        String ans = userBusiness.sayHello(name);
        System.out.println(ans);
        return ans;
    }

}
