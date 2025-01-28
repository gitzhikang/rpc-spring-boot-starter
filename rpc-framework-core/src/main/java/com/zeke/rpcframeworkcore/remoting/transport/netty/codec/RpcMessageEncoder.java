package com.zeke.rpcframeworkcore.remoting.transport.netty.codec;

import com.zeke.rpcframeworkcommon.enums.CompressTypeEnum;
import com.zeke.rpcframeworkcommon.enums.SerializationTypeEnum;
import com.zeke.rpcframeworkcommon.extension.ExtensionLoader;
import com.zeke.rpcframeworkcore.compress.Compress;
import com.zeke.rpcframeworkcore.remoting.constants.RpcConstants;
import com.zeke.rpcframeworkcore.remoting.dto.RpcMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.zeke.rpcframeworkcore.serialize.Serializer;

import java.util.concurrent.atomic.AtomicInteger;

public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {

    private final Logger log = LoggerFactory.getLogger(RpcMessageEncoder.class);
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);


    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage, ByteBuf out) throws Exception {
        try {
            out.writeBytes(RpcConstants.MAGIC_NUMBER);
            out.writeByte(RpcConstants.VERSION);
            // leave a place to write the value of full length
            out.writerIndex(out.writerIndex() + 4);
            byte messageType = rpcMessage.getMessageType();
            out.writeByte(messageType);
            out.writeByte(rpcMessage.getCodec());
            out.writeByte(rpcMessage.getCompress());
            out.writeInt(ATOMIC_INTEGER.getAndIncrement());
            // build full length
            byte[] bodyBytes = null;
            int fullLength = RpcConstants.HEAD_LENGTH;
            //if message type is not heart beat message, then serialize the request object and write it to buffer
            if (messageType != RpcConstants.HEARTBEAT_REQUEST_TYPE
                    && messageType != RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                // serialize the object
                String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
                log.info("codec name: [{}] ", codecName);
                Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                        .getExtension(codecName);
                bodyBytes = serializer.serialize(rpcMessage.getData());
                // compress the bytes
                String compressName = CompressTypeEnum.getName(rpcMessage.getCompress());
                Compress compress = ExtensionLoader.getExtensionLoader(Compress.class)
                        .getExtension(compressName);
                bodyBytes = compress.compress(bodyBytes);
                fullLength += bodyBytes.length;

            }
            if (bodyBytes != null) {
                out.writeBytes(bodyBytes);
            }
            int writeIndex =  out.writerIndex();
            // write full length
            out.writerIndex(writeIndex - fullLength + RpcConstants.MAGIC_NUMBER.length + 1);
            out.writeInt(fullLength);
            out.writerIndex(writeIndex);
        } catch (Exception e) {
            log.error("Encode request error!", e);
        }
    }
}
