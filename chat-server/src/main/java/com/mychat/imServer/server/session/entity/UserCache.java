package com.mychat.imServer.server.session.entity;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class UserCache {
    private String userId;
    private Map<String, SessionCache> map = new LinkedHashMap<>(10);

    public UserCache(String userId) {
        this.userId = userId;
    }

    public void addSession(SessionCache session) {
        map.put(session.getSessionId(), session);
    }

    public void removeSession(String sessionId) {
        map.remove(sessionId);
    }
}