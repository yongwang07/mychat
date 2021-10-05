package com.mychat.imServer.server.session.entity;

import com.mychat.entity.ImNode;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class SessionCache implements Serializable {
    private static final long serialVersionUID = -403010884211394856L;

    private String userId;
    private String sessionId;
    private ImNode imNode;

    public SessionCache() {
        userId = "";
        sessionId = "";
        imNode = new ImNode("unKnown", 0);
    }

    public SessionCache(String sessionId, String userId, ImNode imNode) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.imNode = imNode;
    }
}