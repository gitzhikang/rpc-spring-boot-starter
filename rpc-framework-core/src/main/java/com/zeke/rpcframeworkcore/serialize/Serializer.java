package com.zeke.rpcframeworkcore.serialize;


import com.zeke.rpcframeworkcommon.extension.SPI;

@SPI
public interface Serializer {
    /**
     * serialize
     *
     * @param obj serialize object
     * @return object serialize result
     */
    byte[] serialize(Object obj);

    /**
     * deserialize
     *
     * @param bytes byte array after serialize
     * @param clazz target class
     * @param <T>   type of class。举个例子,  {@code String.class} 的类型是 {@code Class<String>}.
     *              如果不知道类的类型的话，使用 {@code Class<?>}
     * @return deserialize object
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
