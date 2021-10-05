package com.mychat.imClient.client;

import com.mychat.im.common.bean.UserDTO;
import com.mychat.im.common.codec.ProtobufDecoder;
import com.mychat.im.common.codec.ProtobufEncoder;
import com.mychat.imClient.ClientSender.ChatSender;
import com.mychat.imClient.ClientSender.LoginSender;
import com.mychat.imClient.clientHandler.ChatMsgHandler;
import com.mychat.imClient.clientHandler.ExceptionHandler;
import com.mychat.imClient.clientHandler.LoginResponseHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Data
@Service("NettyClient")
public class NettyClient {
    private String host;
    private int port;

    @Autowired
    private ChatMsgHandler chatMsgHandler;

    @Autowired
    private LoginResponseHandler loginResponseHandler;

    @Autowired
    private ExceptionHandler exceptionHandler;

    private Channel channel;
    private ChatSender sender;
    private LoginSender loginSender;

    private boolean initFlag = true;
    private UserDTO user;
    private GenericFutureListener<ChannelFuture> connectedListener;

    private Bootstrap b;
    private EventLoopGroup g;

    public NettyClient() {
        g = new NioEventLoopGroup();
    }

    public void doConnect() {
        try {
            b = new Bootstrap();
            b.group(g);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            b.remoteAddress(host, port);
            b.handler(new ChannelInitializer<SocketChannel>() {
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast("decoder", new ProtobufDecoder());
                            ch.pipeline().addLast("encoder", new ProtobufEncoder());
                            ch.pipeline().addLast("loginResponseHandler", loginResponseHandler);
                            ch.pipeline().addLast("chatMsgHandler", chatMsgHandler);
                            ch.pipeline().addLast("exceptionHandler", exceptionHandler);
                        }
                    }
            );
            ChannelFuture f = b.connect();
            f.addListener(connectedListener);
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    public void close()
    {
        g.shutdownGracefully();
    }
}