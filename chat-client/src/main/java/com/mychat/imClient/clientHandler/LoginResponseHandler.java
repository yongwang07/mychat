package com.mychat.imClient.clientHandler;


import com.mychat.im.common.ProtoInstant;
import com.mychat.im.common.bean.msg.ProtoMsg;
import com.mychat.imClient.client.ClientSession;
import com.mychat.imClient.client.CommandController;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@ChannelHandler.Sharable
public class LoginResponseHandler extends ChannelInboundHandlerAdapter {
    @Autowired
    CommandController commandController;
    @Autowired
    HeartBeatClientHandler heartBeatClientHandler;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        if (null == msg || !(msg instanceof ProtoMsg.Message)) {
            super.channelRead(ctx, msg);
            return;
        }

        ProtoMsg.Message pkg = (ProtoMsg.Message) msg;
        ProtoMsg.HeadType headType = ((ProtoMsg.Message) msg).getType();
        if (!headType.equals(ProtoMsg.HeadType.LOGIN_RESPONSE)) {
            super.channelRead(ctx, msg);
            return;
        }
        ProtoMsg.LoginResponse info = pkg.getLoginResponse();
        ProtoInstant.ResultCodeEnum result = ProtoInstant.ResultCodeEnum.values()[info.getCode()];
        if (!result.equals(ProtoInstant.ResultCodeEnum.SUCCESS)) {
            log.info(result.getDesc());
        } else {
            ClientSession session = ctx.channel().attr(ClientSession.SESSION_KEY).get();
            session.setSessionId(pkg.getSessionId());
            session.setLogin(true);
            commandController.notifyCommandThread();
            ctx.channel().pipeline().addAfter("loginResponseHandler", "heartBeatClientHandler", heartBeatClientHandler);
            heartBeatClientHandler.channelActive(ctx);
            ctx.channel().pipeline().remove("loginResponseHandler");
        }
    }
}