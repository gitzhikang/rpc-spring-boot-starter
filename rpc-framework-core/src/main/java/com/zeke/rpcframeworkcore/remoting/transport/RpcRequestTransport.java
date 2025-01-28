package com.zeke.rpcframeworkcore.remoting.transport;

import com.zeke.rpcframeworkcommon.extension.SPI;
import com.zeke.rpcframeworkcore.remoting.dto.RpcRequest;

@SPI
public interface RpcRequestTransport {
    /**
     * send rpc request to server and get result
     *
     * @param rpcRequest message body
     * @return data from server
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}
