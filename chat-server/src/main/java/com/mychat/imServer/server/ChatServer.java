package com.mychat.imServer.server;

import com.mychat.cocurrent.FutureTaskScheduler;
import com.mychat.im.common.codec.ProtobufDecoder;
import com.mychat.im.common.codec.ProtobufEncoder;
import com.mychat.imServer.distributed.ImWorker;
import com.mychat.imServer.distributed.WorkerRouter;
import com.mychat.imServer.serverHandler.ChatRedirectHandler;
import com.mychat.imServer.serverHandler.LoginRequestHandler;
import com.mychat.imServer.serverHandler.RemoteNotificationHandler;
import com.mychat.imServer.serverHandler.ServerExceptionHandler;
import com.mychat.util.IOUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;

@Data
@Slf4j
@Service("ChatServer")
public class ChatServer {
    @Value("${server.port}")
    private int port;
    private EventLoopGroup bg;
    private EventLoopGroup wg;

    private ServerBootstrap b = new ServerBootstrap();
    @Autowired
    private LoginRequestHandler loginRequestHandler;

    @Autowired
    private ServerExceptionHandler serverExceptionHandler;

    @Autowired
    private RemoteNotificationHandler remoteNotificationHandler;

    @Autowired
    private ChatRedirectHandler chatRedirectHandler;

    public void run() {
        bg = new NioEventLoopGroup(1);
        wg = new NioEventLoopGroup();
        b.group(bg, wg);
        b.channel(NioServerSocketChannel.class);
        String ip = IOUtil.getHostAddress();
        b.localAddress(new InetSocketAddress(ip, port));
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

        b.childHandler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast("deCoder", new ProtobufDecoder());
                ch.pipeline().addLast("enCoder", new ProtobufEncoder());
                ch.pipeline().addLast("login", loginRequestHandler);
                ch.pipeline().addLast("remoteNotificationHandler", remoteNotificationHandler);
                ch.pipeline().addLast("serverException", serverExceptionHandler);
            }
        });
        ChannelFuture channelFuture = null;
        boolean isStart = false;
        while (!isStart) {
            try {
                channelFuture = b.bind().sync();
                log.info("mychat:" + channelFuture.channel().localAddress());
                isStart = true;
            } catch (Exception e) {
                port++;
                b.localAddress(new InetSocketAddress(port));
            }
        }
        ImWorker.getInst().setLocalNode(ip, port);
        FutureTaskScheduler.add(() -> {
            ImWorker.getInst().init();
            WorkerRouter.getInst().init();
        });

        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {
                    wg.shutdownGracefully();
                    bg.shutdownGracefully();
                }));
        try {
            ChannelFuture closeFuture = channelFuture.channel().closeFuture();
            closeFuture.sync();
        } catch (
                Exception e) {
            log.error("exception:", e);
        } finally {
            wg.shutdownGracefully();
            bg.shutdownGracefully();
        }
    }
}