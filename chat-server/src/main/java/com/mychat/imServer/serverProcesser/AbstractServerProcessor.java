package com.mychat.imServer.serverProcesser;

import com.mychat.imServer.server.session.LocalSession;
import io.netty.channel.Channel;

public abstract class AbstractServerProcessor implements ServerReceiver {
    protected String getKey(Channel ch) {
        return ch.attr(LocalSession.KEY_USER_ID).get();
    }

    protected void setKey(Channel ch, String key) {
        ch.attr(LocalSession.KEY_USER_ID).set(key);
    }

    protected void checkAuth(Channel ch) throws Exception {
        if (null == getKey(ch)) {
            throw new Exception("login failed");
        }
    }
}