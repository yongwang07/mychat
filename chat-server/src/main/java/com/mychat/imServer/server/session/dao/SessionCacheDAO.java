package com.mychat.imServer.server.session.dao;


import com.mychat.imServer.server.session.entity.SessionCache;

public interface SessionCacheDAO {
    void save(SessionCache s);

    SessionCache get(String sessionId);

    void remove(String sessionId);
}