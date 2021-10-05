package com.mychat.imServer.server.session;

import com.mychat.constants.ServerConstants;
import com.mychat.im.common.bean.UserDTO;
import com.mychat.imServer.server.session.service.SessionManger;
import com.mychat.util.JsonUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class LocalSession implements ServerSession {
    public static final AttributeKey<String> KEY_USER_ID = AttributeKey.valueOf("key_user_id");
    public static final AttributeKey<LocalSession> SESSION_KEY = AttributeKey.valueOf("SESSION_KEY");

    private Channel channel;
    private UserDTO user;
    private final String sessionId;
    private boolean isLogin = false;
    private Map<String, Object> map = new HashMap<>();

    public LocalSession(Channel channel) {
        this.channel = channel;
        this.sessionId = buildNewSessionId();
    }

    public static LocalSession getSession(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        return channel.attr(LocalSession.SESSION_KEY).get();
    }

    public LocalSession bind() {
        log.info(" LocalSession session bind " + channel.remoteAddress());
        channel.attr(LocalSession.SESSION_KEY).set(this);
        channel.attr(ServerConstants.CHANNEL_NAME).set(JsonUtil.pojoToJson(user));
        isLogin = true;
        return this;
    }

    public LocalSession unbind() {
        isLogin = false;
        SessionManger.inst().removeSession(getSessionId());
        this.close();
        return this;
    }

    public String getSessionId() {
        return sessionId;
    }

    private static String buildNewSessionId() {
        String uuid = UUID.randomUUID().toString();
        return uuid.replaceAll("-", "");
    }

    public synchronized void set(String key, Object value) {
        map.put(key, value);
    }

    public synchronized <T> T get(String key) {
        return (T) map.get(key);
    }

    public boolean isValid() {
        return getUser() != null ? true : false;
    }

    public synchronized void writeAndFlush(Object pkg) {
        if (channel.isWritable()) {
            channel.writeAndFlush(pkg);
        }
    }

    public synchronized void writeAndClose(Object pkg) {
        channel.writeAndFlush(pkg);
        close();
    }

    public synchronized void close() {
        ChannelFuture future = channel.close();
        future.addListener((ChannelFutureListener) future1 -> {
            if (!future1.isSuccess()) {
                log.error("CHANNEL_CLOSED error ");
            }
        });
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
        user.setSessionId(sessionId);
    }

    @Override
    public String getUserId() {
        return user.getUserId();
    }
}