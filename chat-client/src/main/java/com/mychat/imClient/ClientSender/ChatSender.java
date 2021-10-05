package com.mychat.imClient.ClientSender;

import com.mychat.im.common.bean.ChatMsg;
import com.mychat.im.common.bean.msg.ProtoMsg;
import com.mychat.imClient.clientBuilder.ChatMsgBuilder;
import com.mychat.util.Logger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("ChatSender")
public class ChatSender extends BaseSender {
    public void sendChatMsg(String touid, String content) {
        ChatMsg chatMsg = new ChatMsg(getUser());
        chatMsg.setContent(content);
        chatMsg.setMsgType(ChatMsg.MSGTYPE.TEXT);
        chatMsg.setTo(touid);
        chatMsg.setMsgId(System.currentTimeMillis());
        ProtoMsg.Message message =
                ChatMsgBuilder.buildChatMsg(chatMsg, getUser(), getSession());
        super.sendMsg(message);
    }

    @Override
    protected void sendSucceed(ProtoMsg.Message message) {
        Logger.tcfo("chat:"
                + message.getMessageRequest().getContent()
                + "->"
                + message.getMessageRequest().getTo());
    }

    @Override
    protected void sendException(ProtoMsg.Message message) {
        Logger.tcfo("chat exception:"
                + message.getMessageRequest().getContent()
                + "->"
                + message.getMessageRequest().getTo());
    }

    @Override
    protected void sendFailed(ProtoMsg.Message message) {
        Logger.tcfo("chat failed:"
                + message.getMessageRequest().getContent()
                + "->"
                + message.getMessageRequest().getTo());
    }
}