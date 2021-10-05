package com.mychat.imServer.protoBuilder;


import com.mychat.im.common.bean.msg.ProtoMsg;

public class NotificationMsgBuilder {
    public static ProtoMsg.Message buildNotification(String json) {
        ProtoMsg.Message.Builder mb = ProtoMsg.Message.newBuilder()
                .setType(ProtoMsg.HeadType.MESSAGE_NOTIFICATION);
        ProtoMsg.MessageNotification.Builder rb =
                ProtoMsg.MessageNotification.newBuilder()
                        .setJson(json);
        mb.setNotification(rb.build());
        return mb.build();
    }
}