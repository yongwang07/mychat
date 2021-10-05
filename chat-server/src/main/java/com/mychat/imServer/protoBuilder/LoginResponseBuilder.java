package com.mychat.imServer.protoBuilder;

import com.mychat.im.common.ProtoInstant;
import com.mychat.im.common.bean.msg.ProtoMsg;
import org.springframework.stereotype.Service;

@Service("LoginResponseBuilder")
public class LoginResponseBuilder {
    public ProtoMsg.Message loginResponse(
            ProtoInstant.ResultCodeEnum en, long seqId, String sessionId) {
        ProtoMsg.Message.Builder mb = ProtoMsg.Message.newBuilder()
                .setType(ProtoMsg.HeadType.LOGIN_RESPONSE)
                .setSequence(seqId)
                .setSessionId(sessionId);

        ProtoMsg.LoginResponse.Builder b = ProtoMsg.LoginResponse.newBuilder()
                .setCode(en.getCode())
                .setInfo(en.getDesc())
                .setExpose(1);

        mb.setLoginResponse(b.build());
        return mb.build();
    }
}