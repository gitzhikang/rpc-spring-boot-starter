package com.zeke.rpcframeworkcore.config.condition;

public class RpcClientCondition extends RpcAnnotationCondition {
    @Override
    protected String getAnnotationClassName() {
        return "com.zeke.rpcframeworkcore.annotation.RpcClient";
    }

}
