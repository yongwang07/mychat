package com.mychat.imServer.server.session.dao;


import com.mychat.imServer.server.session.entity.SessionCache;
import com.mychat.imServer.server.session.entity.UserCache;

public interface UserCacheDAO {
    void save(UserCache s);

    UserCache get(String userId);

    void addSession(String uid, SessionCache session);

    void removeSession(String uid, String sessionId);
}