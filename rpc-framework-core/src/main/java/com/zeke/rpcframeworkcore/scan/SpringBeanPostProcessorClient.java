package com.zeke.rpcframeworkcore.scan;

import com.zeke.rpcframeworkcore.annotation.RpcReference;
import com.zeke.rpcframeworkcore.annotation.RpcService;
import com.zeke.rpcframeworkcore.config.RpcServiceConfig;
import com.zeke.rpcframeworkcore.provider.ServiceProvider;
import com.zeke.rpcframeworkcore.proxy.RpcClientProxy;
import com.zeke.rpcframeworkcore.remoting.transport.RpcRequestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;

public class SpringBeanPostProcessorClient implements BeanPostProcessor {

    private final ServiceProvider serviceProvider;
    private final RpcRequestTransport rpcClient;
    private final Logger log = LoggerFactory.getLogger(SpringBeanPostProcessorClient.class);

    public SpringBeanPostProcessorClient(ServiceProvider serviceProvider, RpcRequestTransport rpcClient) {
        this.serviceProvider = serviceProvider;
        this.rpcClient = rpcClient;
    }


    //inject proxy object for field annotated with @RpcReference
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        Field[] declaredFields = targetClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            RpcReference rpcReference = declaredField.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                        .group(rpcReference.group())
                        .version(rpcReference.version()).build();
                RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient, rpcServiceConfig);
                Object clientProxy = rpcClientProxy.getProxy(declaredField.getType());
                declaredField.setAccessible(true);
                try {
                    declaredField.set(bean, clientProxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

        }
        return bean;
    }


}
