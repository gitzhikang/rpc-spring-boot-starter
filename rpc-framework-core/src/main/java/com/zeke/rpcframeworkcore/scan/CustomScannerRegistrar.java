package com.zeke.rpcframeworkcore.scan;

import com.zeke.rpcframeworkcore.annotation.RpcScan;
import com.zeke.rpcframeworkcore.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * scan and filter specified annotations
 *
 * @author shuang.kou
 * @createTime 2020年08月10日 22:12:00
 */
@Slf4j
public class CustomScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {
    private static final String BASE_PACKAGE_ATTRIBUTE_NAME = "basePackage";
    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;

    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        //get the attributes and values of RpcScan annotation from spring application startup class
        AnnotationAttributes rpcScanAnnotationAttributes = AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(RpcScan.class.getName()));
        String[] rpcScanBasePackages = new String[0];
        // scan path for Component
        String[] componentBasePackages = getBasePackages(annotationMetadata);
        if (rpcScanAnnotationAttributes != null) {
            // get the value of the basePackage property
            rpcScanBasePackages = rpcScanAnnotationAttributes.getStringArray(BASE_PACKAGE_ATTRIBUTE_NAME);
        }
        if (rpcScanBasePackages.length == 0) {
            rpcScanBasePackages = new String[]{((StandardAnnotationMetadata) annotationMetadata).getIntrospectedClass().getPackage().getName()};
        }
        // Scan the RpcService annotation
        CustomScanner rpcServiceScanner = new CustomScanner(beanDefinitionRegistry, RpcService.class);
        // Scan the Component annotation
        CustomScanner springBeanScanner = new CustomScanner(beanDefinitionRegistry, Component.class);
        if (resourceLoader != null) {
            rpcServiceScanner.setResourceLoader(resourceLoader);
            springBeanScanner.setResourceLoader(resourceLoader);
        }

        int springBeanAmount = springBeanScanner.scan(componentBasePackages);
        log.info("springBeanScanner扫描的数量 [{}]", springBeanAmount);
        int rpcServiceCount = rpcServiceScanner.scan(rpcScanBasePackages);
        log.info("rpcServiceScanner扫描的数量 [{}]", rpcServiceCount);

    }

    private String[] getBasePackages(AnnotationMetadata annotationMetadata) {
        // 首先检查是否配置了@ComponentScan
        AnnotationAttributes componentScanAttributes = AnnotationAttributes.fromMap(
                annotationMetadata.getAnnotationAttributes(ComponentScan.class.getName()));

        if (componentScanAttributes != null) {
            // 获取@ComponentScan中配置的basePackages
            String[] basePackages = componentScanAttributes.getStringArray("basePackages");
            if (basePackages != null && basePackages.length > 0) {
                return basePackages;
            }

            // 如果basePackages为空，检查basePackageClasses
            Class<?>[] basePackageClasses = componentScanAttributes.getClassArray("basePackageClasses");
            if (basePackageClasses != null && basePackageClasses.length > 0) {
                return Arrays.stream(basePackageClasses)
                        .map(Class::getPackage)
                        .map(Package::getName)
                        .toArray(String[]::new);
            }
        }

        // 如果没有配置@ComponentScan或配置为空，使用启动类所在包作为默认值
        return new String[]{((StandardAnnotationMetadata) annotationMetadata)
                .getIntrospectedClass().getPackage().getName()};
    }

}
