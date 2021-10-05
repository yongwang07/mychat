package com.mychat.imServer.serverProcesser;

import com.mychat.im.common.bean.msg.ProtoMsg;
import com.mychat.imServer.server.session.LocalSession;
import com.mychat.imServer.server.session.ServerSession;
import com.mychat.imServer.server.session.service.SessionManger;
import com.mychat.util.Logger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service("ChatRedirectProcessor")
public class ChatRedirectProcessor extends AbstractServerProcessor {

    @Override
    public ProtoMsg.HeadType op() {
        return ProtoMsg.HeadType.MESSAGE_REQUEST;
    }

    @Override
    public Boolean action(LocalSession fromSession, ProtoMsg.Message proto) {
        ProtoMsg.MessageRequest messageRequest = proto.getMessageRequest();
        Logger.tcfo("chatMsg | from="
                + messageRequest.getFrom()
                + " , to =" + messageRequest.getTo()
                + " , MsgType =" + messageRequest.getMsgType()
                + " , content =" + messageRequest.getContent());

        String to = messageRequest.getTo();
        List<ServerSession> toSessions = SessionManger.inst().getSessionsBy(to);
        if (toSessions == null) {
            //TODO
            Logger.tcfo("[" + to + "] offline，save to hbase!");
        } else {
            toSessions.forEach((session) -> {
                session.writeAndFlush(proto);
            });
        }
        return null;
    }
}