package com.zeke.rpcframeworkcore.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rpc.config", ignoreInvalidFields = true)
public class PrcFrameworkAutoProperties {

    private int baseSleepTime = 1000;
    private int maxRetries = 3;
    public String zkRegisterRootPath = "/my-rpc";
    private String defaultZookeeperAddress = "127.0.0.1:2181";

    // 添加 getter 和 setter 方法
    public int getBaseSleepTime() {
        return baseSleepTime;
    }

    public int getMaxRetries() {
        return maxRetries;
    }


    public String getZkRegisterRootPath() {
        return zkRegisterRootPath;
    }


    public String getDefaultZookeeperAddress() {
        return defaultZookeeperAddress;
    }



}
