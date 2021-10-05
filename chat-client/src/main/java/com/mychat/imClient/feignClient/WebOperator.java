package com.mychat.imClient.feignClient;

import com.mychat.constants.ServerConstants;
import com.mychat.entity.LoginBack;
import com.mychat.util.JsonUtil;
import feign.Feign;
import feign.codec.StringDecoder;

public class WebOperator {
    public static LoginBack login(String userName, String password) {
        UserAction action = Feign.builder()
                .decoder(new StringDecoder())
                .target(UserAction.class, ServerConstants.WEB_URL);
        String s = action.loginAction(userName, password);
        LoginBack back = JsonUtil.jsonToPojo(s, LoginBack.class);
        return back;
    }
}