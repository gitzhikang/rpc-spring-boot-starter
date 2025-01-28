package com.zeke.rpcframeworkcore.remoting.transport.netty.server;

import com.zeke.rpcframeworkcommon.factory.SingletonFactory;
import com.zeke.rpcframeworkcommon.utils.RuntimeUtil;
import com.zeke.rpcframeworkcommon.utils.threadpool.ThreadPoolFactoryUtil;
import com.zeke.rpcframeworkcore.config.CustomShutdownHook;
import com.zeke.rpcframeworkcore.config.RpcServiceConfig;
import com.zeke.rpcframeworkcore.provider.ServiceProvider;
import com.zeke.rpcframeworkcore.provider.impl.ZkServiceProviderImpl;
import com.zeke.rpcframeworkcore.remoting.transport.netty.codec.RpcMessageEncoder;
import com.zeke.rpcframeworkcore.remoting.transport.netty.codec.RpcMessageDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

public class NettyRpcServer {

    public final Logger log = LoggerFactory.getLogger(NettyRpcServer.class);

    public static final int PORT = 9998;

    private final ServiceProvider serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);

    // Register service
    public void registerService(RpcServiceConfig rpcServiceConfig) {
        serviceProvider.publishService(rpcServiceConfig);
    }

    // Start server
    @SneakyThrows
    public void start(){
        //add shutdown hook to clear the service in zoopkeeper
        CustomShutdownHook.getCustomShutdownHook().clearAll();
        String host = InetAddress.getLocalHost().getHostAddress();
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        //serviceHandlerGroup is used to execute the user-defined tasks（invoke function）
        DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(
                RuntimeUtil.cpus() * 2,
                ThreadPoolFactoryUtil.createThreadFactory("service-handler-group", false)
        );
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            // 30 秒之内没有收到客户端请求的话就关闭连接
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                            p.addLast(new RpcMessageEncoder());
                            p.addLast(new RpcMessageDecoder());
                            p.addLast(serviceHandlerGroup, new NettyRpcServerHandler());
                        }
                    });

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(host, PORT).sync();
            // Wait until the server socket is closed.
            f.channel().closeFuture().sync();
        }catch (Exception e){
            log.error("An error occurred while starting the server",e);
        }finally {
            log.error("shutdown bossGroup and workerGroup");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            serviceHandlerGroup.shutdownGracefully();
        }
    }


}
