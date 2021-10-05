package com.mychat.imClient.clientBuilder;

import com.mychat.im.common.bean.ChatMsg;
import com.mychat.im.common.bean.UserDTO;
import com.mychat.im.common.bean.msg.ProtoMsg;
import com.mychat.imClient.client.ClientSession;

public class ChatMsgBuilder extends BaseBuilder {
    private ChatMsg chatMsg;
    private UserDTO user;

    public ChatMsgBuilder(
            ChatMsg chatMsg,
            UserDTO user,
            ClientSession session) {
        super(ProtoMsg.HeadType.MESSAGE_REQUEST, session);
        this.chatMsg = chatMsg;
        this.user = user;
    }

    public ProtoMsg.Message build() {
        ProtoMsg.Message message = buildCommon(-1);
        ProtoMsg.MessageRequest.Builder cb
                = ProtoMsg.MessageRequest.newBuilder();
        chatMsg.fillMsg(cb);
        return message
                .toBuilder()
                .setMessageRequest(cb)
                .build();
    }

    public static ProtoMsg.Message buildChatMsg(
            ChatMsg chatMsg,
            UserDTO user,
            ClientSession session) {
        ChatMsgBuilder builder =
                new ChatMsgBuilder(chatMsg, user, session);
        return builder.build();
    }
}