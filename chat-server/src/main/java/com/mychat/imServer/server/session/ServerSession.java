package com.mychat.imServer.server.session;

public interface ServerSession {
    void writeAndFlush(Object pkg);

    String getSessionId();

    boolean isValid();
    String getUserId();
}