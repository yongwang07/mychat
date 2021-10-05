package com.mychat.imServer.serverHandler;

import com.mychat.cocurrent.CallbackTask;
import com.mychat.cocurrent.CallbackTaskScheduler;
import com.mychat.im.common.bean.msg.ProtoMsg;
import com.mychat.imServer.server.session.LocalSession;
import com.mychat.imServer.server.session.service.SessionManger;
import com.mychat.imServer.serverProcesser.LoginProcessor;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service("LoginRequestHandler")
@ChannelHandler.Sharable
public class LoginRequestHandler extends ChannelInboundHandlerAdapter {
    @Autowired
    LoginProcessor loginProcesser;
    @Autowired
    private ChatRedirectHandler chatRedirectHandler;
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (null == msg || !(msg instanceof ProtoMsg.Message)) {
            super.channelRead(ctx, msg);
            return;
        }
        ProtoMsg.Message pkg = (ProtoMsg.Message) msg;
        ProtoMsg.HeadType headType = pkg.getType();
        if (!headType.equals(loginProcesser.op())) {
            super.channelRead(ctx, msg);
            return;
        }
        LocalSession session = new LocalSession(ctx.channel());
        CallbackTaskScheduler.add(new CallbackTask<Boolean>() {
            @Override
            public Boolean execute() throws Exception {
                return loginProcesser.action(session, pkg);
            }
            @Override
            public void onBack(Boolean r) {
                if (r) {
                    log.info("login succeed:" + session.getUser());
                    ctx.pipeline().addAfter("login", "chat",   chatRedirectHandler);
                    ctx.pipeline().addAfter("login", "heartBeat",new HeartBeatServerHandler());
                    ctx.pipeline().remove("login");
                } else {
                    SessionManger.inst().closeSession(ctx);
                    log.info("login failed:" + session.getUser());
                }
            }
            @Override
            public void onException(Throwable t) {
                t.printStackTrace();
                log.info("login failed:" + session.getUser());
                SessionManger.inst().closeSession(ctx);
            }
        });
    }
}