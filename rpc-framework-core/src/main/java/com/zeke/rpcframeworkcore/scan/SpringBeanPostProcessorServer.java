package com.zeke.rpcframeworkcore.scan;

import com.zeke.rpcframeworkcore.annotation.RpcService;
import com.zeke.rpcframeworkcore.config.RpcServiceConfig;
import com.zeke.rpcframeworkcore.provider.ServiceProvider;
import com.zeke.rpcframeworkcore.remoting.transport.RpcRequestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class SpringBeanPostProcessorServer implements BeanPostProcessor {

    private final ServiceProvider serviceProvider;
    private final Logger log = LoggerFactory.getLogger(SpringBeanPostProcessorServer.class);

    public SpringBeanPostProcessorServer(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }


    //register service for class annotated with @RpcService
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            log.info("[{}] is annotated with  [{}]", bean.getClass().getName(), RpcService.class.getCanonicalName());
            // get RpcService annotation
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            // build RpcServiceProperties
            RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                    .group(rpcService.group())
                    .version(rpcService.version())
                    .service(bean).build();
            serviceProvider.publishService(rpcServiceConfig);
        }
        return bean;
    }
}
