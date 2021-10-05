package com.mychat.imClient.clientBuilder;

import com.mychat.im.common.bean.UserDTO;
import com.mychat.im.common.bean.msg.ProtoMsg;
import com.mychat.imClient.client.ClientSession;


public class LoginMsgBuilder extends BaseBuilder {
    private final UserDTO user;

    public LoginMsgBuilder(
            UserDTO user,
            ClientSession session) {
        super(ProtoMsg.HeadType.LOGIN_REQUEST, session);
        this.user = user;
    }

    public ProtoMsg.Message build() {
        ProtoMsg.Message message = buildCommon(-1);
        ProtoMsg.LoginRequest.Builder lb =
                ProtoMsg.LoginRequest.newBuilder()
                        .setDeviceId(user.getDevId())
                        .setPlatform(user.getPlatform().ordinal())
                        .setToken(user.getToken())
                        .setUid(user.getUserId());
        return message.toBuilder().setLoginRequest(lb).build();
    }

    public static ProtoMsg.Message buildLoginMsg(
            UserDTO user,
            ClientSession session) {
        LoginMsgBuilder builder = new LoginMsgBuilder(user, session);
        return builder.build();
    }
}