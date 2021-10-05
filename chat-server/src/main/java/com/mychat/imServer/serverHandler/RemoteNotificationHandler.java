package com.mychat.imServer.serverHandler;

import com.mychat.constants.ServerConstants;
import com.mychat.entity.ImNode;
import com.mychat.im.common.bean.Notification;
import com.mychat.im.common.bean.msg.ProtoMsg;
import com.mychat.imServer.server.session.LocalSession;
import com.mychat.imServer.server.session.service.SessionManger;
import com.mychat.util.JsonUtil;
import com.google.gson.reflect.TypeToken;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("RemoteNotificationHandler")
@ChannelHandler.Sharable
public class RemoteNotificationHandler extends ChannelInboundHandlerAdapter {
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (null == msg || !(msg instanceof ProtoMsg.Message)) {
            super.channelRead(ctx, msg);
            return;
        }
        ProtoMsg.Message pkg = (ProtoMsg.Message) msg;
        ProtoMsg.HeadType headType = pkg.getType();
        if (!headType.equals(ProtoMsg.HeadType.MESSAGE_NOTIFICATION)) {
            super.channelRead(ctx, msg);
            return;
        }

        ProtoMsg.MessageNotification notificationPkg = pkg.getNotification();
        String json = notificationPkg.getJson();

        Notification<Notification.ContentWrapper> notification =
                JsonUtil.jsonToPojo(json, new TypeToken<Notification<Notification.ContentWrapper>>()
                {}.getType());

        if (notification.getType() == Notification.SESSION_OFF) {
            String sid = notification.getWrapperContent();
            SessionManger.inst().removeRemoteSession(sid);
        }
        if (notification.getType() == Notification.SESSION_ON)
        {
            String sid = notification.getWrapperContent();
            //TODO
//            SessionManger.inst().addRemoteSession(remoteSession);
        }

        if (notification.getType() == Notification.CONNECT_FINISHED) {
            Notification<ImNode> nodInfo =
                    JsonUtil.jsonToPojo(json, new TypeToken<Notification<ImNode>>()
                    {}.getType());
            ctx.pipeline().remove("login");
            ctx.channel().attr(ServerConstants.CHANNEL_NAME).set(JsonUtil.pojoToJson(nodInfo));
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LocalSession session = LocalSession.getSession(ctx);
        if (null != session) {
            session.unbind();
        }
    }
}