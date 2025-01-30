package com.zeke.rpcframeworkcore.config;


import com.zeke.rpcframeworkcommon.factory.SingletonFactory;
import com.zeke.rpcframeworkcore.config.condition.RpcClientCondition;
import com.zeke.rpcframeworkcore.config.condition.RpcServerCondition;
import com.zeke.rpcframeworkcore.provider.ServiceProvider;
import com.zeke.rpcframeworkcore.provider.impl.ZkServiceProviderImpl;
import com.zeke.rpcframeworkcore.registry.zk.util.CuratorUtils;
import com.zeke.rpcframeworkcore.remoting.transport.netty.client.NettyRpcClient;
import com.zeke.rpcframeworkcore.remoting.transport.netty.server.NettyRpcServer;
import com.zeke.rpcframeworkcore.scan.SpringBeanPostProcessorClient;
import com.zeke.rpcframeworkcore.scan.SpringBeanPostProcessorServer;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.annotation.PostConstruct;

@Configuration
@EnableConfigurationProperties(PrcFrameworkAutoProperties.class)
public class RpcFrameworkAutoConfig {

    Logger log = org.slf4j.LoggerFactory.getLogger(RpcFrameworkAutoConfig.class);

    @Bean
    ServiceProvider serviceProvider(){
        return SingletonFactory.getInstance(ZkServiceProviderImpl.class);
    }

    @Bean
    @Conditional(RpcClientCondition.class)
    public NettyRpcClient nettyRpcClient(){
        return SingletonFactory.getInstance(NettyRpcClient.class);
    }

    @Bean
    @Conditional(RpcServerCondition.class)
    public NettyRpcServer nettyRpcServer() {
        NettyRpcServer nettyRpcServer = SingletonFactory.getInstance(NettyRpcServer.class);
        new Thread(()->nettyRpcServer.start()).start();
        return nettyRpcServer;
    }

    @Bean
    @Conditional(RpcClientCondition.class)
    @DependsOn({"nettyRpcClient","serviceProvider"})
    public SpringBeanPostProcessorClient springBeanPostProcessorClient(ServiceProvider serviceProvider, NettyRpcClient nettyRpcClient){
        return new SpringBeanPostProcessorClient(serviceProvider,nettyRpcClient);
    }

    @Bean
    @Conditional(RpcServerCondition.class)
    @DependsOn("serviceProvider")
    public SpringBeanPostProcessorServer springBeanPostProcessorServer(ServiceProvider serviceProvider){
        return new SpringBeanPostProcessorServer(serviceProvider);

    }


}
