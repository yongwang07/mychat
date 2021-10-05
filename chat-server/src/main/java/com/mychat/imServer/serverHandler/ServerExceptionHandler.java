package com.mychat.imServer.serverHandler;

import com.mychat.im.common.exception.InvalidFrameException;
import com.mychat.imServer.server.session.service.SessionManger;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@ChannelHandler.Sharable
@Service("ServerExceptionHandler")
public class ServerExceptionHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (!(cause instanceof InvalidFrameException)) {
            cause.printStackTrace();
        }
        log.error(cause.getMessage());
        SessionManger.inst().closeSession(ctx);
        ctx.close();
    }

    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        SessionManger.inst().closeSession(ctx);
    }
}