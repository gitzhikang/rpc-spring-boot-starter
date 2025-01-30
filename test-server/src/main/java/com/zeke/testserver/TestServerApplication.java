package com.zeke.testserver;

import com.zeke.rpcframeworkcore.annotation.RpcScan;
import com.zeke.rpcframeworkcore.annotation.RpcServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RpcServer
@RpcScan(basePackage = {"com.zeke.testserver"})
public class TestServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestServerApplication.class, args);
    }

}
