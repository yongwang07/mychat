package com.mychat.imClient.ClientSender;

import com.mychat.im.common.bean.msg.ProtoMsg;
import com.mychat.imClient.clientBuilder.LoginMsgBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("LoginSender")
public class LoginSender extends BaseSender {
    public void sendLoginMsg() {
        if (!isConnected()) {
            return;
        }
        ProtoMsg.Message message =
                LoginMsgBuilder.buildLoginMsg(getUser(), getSession());
        super.sendMsg(message);
    }
}