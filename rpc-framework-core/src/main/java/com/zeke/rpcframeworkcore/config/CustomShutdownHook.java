package com.zeke.rpcframeworkcore.config;


import com.zeke.rpcframeworkcommon.utils.threadpool.ThreadPoolFactoryUtil;
import com.zeke.rpcframeworkcore.registry.zk.util.CuratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.zeke.rpcframeworkcore.remoting.transport.netty.server.NettyRpcServer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * When the server  is closed, do something such as unregister all services
 *
 * @author shuang.kou
 * @createTime 2020年06月04日 13:11:00
 */

public class CustomShutdownHook {
    private final Logger log = LoggerFactory.getLogger(CustomShutdownHook.class);

    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

    public static CustomShutdownHook getCustomShutdownHook() {
        return CUSTOM_SHUTDOWN_HOOK;
    }

    public void clearAll() {
        log.info("addShutdownHook for clearAll");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), NettyRpcServer.PORT);
                CuratorUtils.clearRegistry(CuratorUtils.getZkClient(), inetSocketAddress);
            } catch (UnknownHostException ignored) {
            }
            ThreadPoolFactoryUtil.shutDownAllThreadPool();
        }));
    }
}
