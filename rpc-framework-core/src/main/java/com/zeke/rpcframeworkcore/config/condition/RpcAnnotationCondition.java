package com.zeke.rpcframeworkcore.config.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.lang.annotation.Annotation;

public abstract class RpcAnnotationCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // 获取主启动类的包名
        String mainClassName = System.getProperty("sun.java.command");
        // 有时启动类名可能包含命令行参数，需要处理一下
        if (mainClassName != null && mainClassName.contains(" ")) {
            mainClassName = mainClassName.split(" ")[0];
        }

        try {
            // 加载启动类
            Class<?> mainClass = Class.forName(mainClassName);

            // 获取启动类上的指定注解
            String targetAnnotation = getAnnotationClassName();
            return mainClass.isAnnotationPresent(
                    (Class<? extends Annotation>) Class.forName(targetAnnotation)
            );

        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    protected abstract String getAnnotationClassName();
}
