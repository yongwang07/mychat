package com.mychat.imServer.serverHandler;

import com.mychat.cocurrent.FutureTaskScheduler;
import com.mychat.im.common.bean.msg.ProtoMsg;
import com.mychat.imServer.server.session.LocalSession;
import com.mychat.imServer.server.session.ServerSession;
import com.mychat.imServer.server.session.service.SessionManger;
import com.mychat.imServer.serverProcesser.ChatRedirectProcessor;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service("ChatRedirectHandler")
@ChannelHandler.Sharable
public class ChatRedirectHandler extends ChannelInboundHandlerAdapter {
    @Autowired
    ChatRedirectProcessor redirectProcessor;

    @Autowired
    SessionManger sessionManger;

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (null == msg || !(msg instanceof ProtoMsg.Message)) {
            super.channelRead(ctx, msg);
            return;
        }
        ProtoMsg.Message pkg = (ProtoMsg.Message) msg;
        ProtoMsg.HeadType headType = ((ProtoMsg.Message) msg).getType();
        if (!headType.equals(redirectProcessor.op())) {
            super.channelRead(ctx, msg);
            return;
        }
        FutureTaskScheduler.add(() -> {
            LocalSession session = LocalSession.getSession(ctx);
            if (null != session && session.isValid()) {
                redirectProcessor.action(session, pkg);
                return;
            }
            ProtoMsg.MessageRequest request = pkg.getMessageRequest();
            List<ServerSession> toSessions = SessionManger.inst().getSessionsBy(request.getTo());
            toSessions.forEach((serverSession) -> {
                if (serverSession instanceof LocalSession) {
                    serverSession.writeAndFlush(pkg);
                }
            });
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LocalSession session = ctx.channel().attr(LocalSession.SESSION_KEY).get();
        if (null != session && session.isValid()) {
            session.close();
            sessionManger.removeSession(session.getSessionId());
        }
    }
}