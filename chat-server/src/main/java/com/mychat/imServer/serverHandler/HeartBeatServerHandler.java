package com.mychat.imServer.serverHandler;

import com.mychat.cocurrent.FutureTaskScheduler;
import com.mychat.constants.ServerConstants;
import com.mychat.im.common.bean.msg.ProtoMsg;
import com.mychat.imServer.server.session.service.SessionManger;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class HeartBeatServerHandler extends IdleStateHandler {
    private static final int READ_IDLE_GAP = 1500;
    public HeartBeatServerHandler() {
        super(READ_IDLE_GAP, 0, 0, TimeUnit.SECONDS);
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (null == msg || !(msg instanceof ProtoMsg.Message)) {
            super.channelRead(ctx, msg);
            return;
        }

        ProtoMsg.Message pkg = (ProtoMsg.Message) msg;
        ProtoMsg.HeadType headType = pkg.getType();
        if (headType.equals(ProtoMsg.HeadType.HEART_BEAT)) {
            FutureTaskScheduler.add(() -> {
                if (ctx.channel().isActive()) {
                    ctx.writeAndFlush(msg);
                }
            });
        }
        super.channelRead(ctx, msg);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        log.info(READ_IDLE_GAP + "no data read, close connection", ctx.channel().attr(ServerConstants.CHANNEL_NAME).get());
        SessionManger.inst().closeSession(ctx);
    }
}