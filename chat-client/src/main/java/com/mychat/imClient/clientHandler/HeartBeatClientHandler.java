package com.mychat.imClient.clientHandler;


import com.mychat.im.common.bean.UserDTO;
import com.mychat.im.common.bean.msg.ProtoMsg;
import com.mychat.imClient.client.ClientSession;
import com.mychat.imClient.protoBuilder.HeartBeatMsgBuilder;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@ChannelHandler.Sharable
@Service("HeartBeatClientHandler")
public class HeartBeatClientHandler extends ChannelInboundHandlerAdapter {
    private static final int HEARTBEAT_INTERVAL = 50;

    @Override
    public void channelActive(ChannelHandlerContext ctx)
            throws Exception {
        ClientSession session = ClientSession.getSession(ctx);
        UserDTO user = session.getUser();
        HeartBeatMsgBuilder builder =
                new HeartBeatMsgBuilder(user, session);
        ProtoMsg.Message message = builder.buildMsg();
        heartBeat(ctx, message);
    }

    public void heartBeat(ChannelHandlerContext ctx,
                          ProtoMsg.Message heartbeatMsg) {
        ctx.executor().schedule(() -> {
            if (ctx.channel().isActive()) {
                ctx.writeAndFlush(heartbeatMsg);
                heartBeat(ctx, heartbeatMsg);
            }
        }, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (null == msg || !(msg instanceof ProtoMsg.Message)) {
            super.channelRead(ctx, msg);
            return;
        }
        ProtoMsg.Message pkg = (ProtoMsg.Message) msg;
        ProtoMsg.HeadType headType = pkg.getType();
        if (headType.equals(ProtoMsg.HeadType.HEART_BEAT)) {
            return;
        } else {
            super.channelRead(ctx, msg);
        }
    }
}