package com.mychat.imServer.serverProcesser;

import com.mychat.im.common.ProtoInstant;
import com.mychat.im.common.bean.UserDTO;
import com.mychat.im.common.bean.msg.ProtoMsg;
import com.mychat.imServer.protoBuilder.LoginResponseBuilder;
import com.mychat.imServer.server.session.LocalSession;
import com.mychat.imServer.server.session.service.SessionManger;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Data
@Slf4j
@Service("LoginProcessor")
public class LoginProcessor extends AbstractServerProcessor {
    @Autowired
    LoginResponseBuilder loginResponseBuilder;
    @Autowired
    SessionManger sessionManger;

    @Override
    public ProtoMsg.HeadType op() {
        return ProtoMsg.HeadType.LOGIN_REQUEST;
    }

    @Override
    public Boolean action(LocalSession session, ProtoMsg.Message proto) {
        ProtoMsg.LoginRequest info = proto.getLoginRequest();
        long seqNo = proto.getSequence();

        UserDTO user = UserDTO.fromMsg(info);

        boolean isValidUser = checkUser(user);
        if (!isValidUser) {
            ProtoInstant.ResultCodeEnum resultCode =
                    ProtoInstant.ResultCodeEnum.NO_TOKEN;
            ProtoMsg.Message response =
                    loginResponseBuilder.loginResponse(resultCode, seqNo, "-1");
            session.writeAndClose(response);
            return false;
        }
        session.setUser(user);
        session.bind();
        sessionManger.addLocalSession(session);

        ProtoInstant.ResultCodeEnum resultCode = ProtoInstant.ResultCodeEnum.SUCCESS;
        ProtoMsg.Message response =
                loginResponseBuilder.loginResponse(resultCode, seqNo, session.getSessionId());
        session.writeAndFlush(response);
        return true;
    }

    private boolean checkUser(UserDTO user) {
        //TODO
        return true;
    }
}