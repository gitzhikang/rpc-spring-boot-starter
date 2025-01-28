package com.zeke.rpcframeworkcore.config;


import com.zeke.rpcframeworkcore.config.condition.RpcClientCondition;
import com.zeke.rpcframeworkcore.config.condition.RpcServerCondition;
import com.zeke.rpcframeworkcore.provider.ServiceProvider;
import com.zeke.rpcframeworkcore.provider.impl.ZkServiceProviderImpl;
import com.zeke.rpcframeworkcore.remoting.transport.netty.client.NettyRpcClient;
import com.zeke.rpcframeworkcore.remoting.transport.netty.server.NettyRpcServer;
import com.zeke.rpcframeworkcore.scan.SpringBeanPostProcessorClient;
import com.zeke.rpcframeworkcore.scan.SpringBeanPostProcessorServer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@EnableConfigurationProperties(PrcFrameworkAutoProperties.class)
public class RpcFrameworkAutoConfig {

    @Bean
    ServiceProvider serviceProvider(){
        return new ZkServiceProviderImpl();
    }

    @Bean
    @Conditional(RpcClientCondition.class)
    public NettyRpcClient nettyRpcClient(){
        return new NettyRpcClient();
    }

    @Bean
    @Conditional(RpcServerCondition.class)
    public NettyRpcServer nettyRpcServer() {
        NettyRpcServer nettyRpcServer = new NettyRpcServer();
        nettyRpcServer.start();
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
