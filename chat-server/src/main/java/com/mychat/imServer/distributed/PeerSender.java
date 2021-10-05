package com.mychat.imServer.distributed;

import com.mychat.entity.ImNode;
import com.mychat.im.common.bean.Notification;
import com.mychat.im.common.bean.UserDTO;
import com.mychat.im.common.bean.msg.ProtoMsg;
import com.mychat.im.common.codec.ProtobufDecoder;
import com.mychat.im.common.codec.ProtobufEncoder;
import com.mychat.imServer.protoBuilder.NotificationMsgBuilder;
import com.mychat.imServer.serverHandler.ImNodeExceptionHandler;
import com.mychat.imServer.serverHandler.ImNodeHeartBeatClientHandler;
import com.mychat.util.JsonUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Data
public class PeerSender {
    private  int reConnectCount=0;
    private Channel channel;
    private ImNode rmNode;
    private boolean connectFlag = false;
    private UserDTO user;

    GenericFutureListener<ChannelFuture> closeListener = (ChannelFuture f) -> {
        channel = null;
        connectFlag = false;
    };

    private GenericFutureListener<ChannelFuture> connectedListener = (ChannelFuture f) -> {
        final EventLoop eventLoop = f.channel().eventLoop();
        if (!f.isSuccess() && ++reConnectCount<3) {
            log.info(String.format("connect failed retry %d after 10s!",reConnectCount));
            eventLoop.schedule(() -> PeerSender.this.doConnect(), 10, TimeUnit.SECONDS);
            connectFlag = false;
        } else {
            connectFlag = true;
            log.info(new Date() + "connected succeed:{}", rmNode.toString());

            channel = f.channel();
            channel.closeFuture().addListener(closeListener);

            Notification<ImNode> notification = new Notification<>(ImWorker.getInst().getLocalNodeInfo());
            notification.setType(Notification.CONNECT_FINISHED);
            String json = JsonUtil.pojoToJson(notification);
            ProtoMsg.Message pkg = NotificationMsgBuilder.buildNotification(json);
            writeAndFlush(pkg);
        }
    };

    private Bootstrap b;
    private EventLoopGroup g;

    public PeerSender(ImNode n) {
        this.rmNode = n;
        b = new Bootstrap();
        g = new NioEventLoopGroup();
    }

    public void doConnect() {
        String host = rmNode.getHost();
        int port = rmNode.getPort();
        try {
            if (b != null && b.group() == null) {
                b.group(g);
                b.channel(NioSocketChannel.class);
                b.option(ChannelOption.SO_KEEPALIVE, true);
                b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
                b.remoteAddress(host, port);
                b.handler(new ChannelInitializer<SocketChannel>() {
                            public void initChannel(SocketChannel ch) {
                                ch.pipeline().addLast("decoder", new ProtobufDecoder());
                                ch.pipeline().addLast("encoder", new ProtobufEncoder());
                                ch.pipeline().addLast("imNodeHeartBeatClientHandler", new ImNodeHeartBeatClientHandler());
                                ch.pipeline().addLast("exceptionHandler", new ImNodeExceptionHandler());
                            }
                        }
                );
                ChannelFuture f = b.connect();
                f.addListener(connectedListener);
                 f.channel().closeFuture().sync();
            } else if (b.group() != null) {
                ChannelFuture f = b.connect();
                f.addListener(connectedListener);
            }
        } catch (Exception e) {
            log.info("connect failed!" + e.getMessage());
        }
    }

    public void stopConnecting() {
        g.shutdownGracefully();
        connectFlag = false;
    }

    public void writeAndFlush(Object pkg) {
        if (connectFlag == false) {
            log.error("node connected failed:", rmNode.toString());
            return;
        }
        channel.writeAndFlush(pkg);
    }
}