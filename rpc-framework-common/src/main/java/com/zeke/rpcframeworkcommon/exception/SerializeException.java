package com.zeke.rpcframeworkcommon.exception;

/**
 * @author shuang.kou
 * @createTime 2020年05月13日 19:54:00
 */
public class SerializeException extends RuntimeException {
    public SerializeException(String message) {
        super(message);
    }
    public SerializeException(String message, Throwable cause) {
        super(message, cause);
    }
}
