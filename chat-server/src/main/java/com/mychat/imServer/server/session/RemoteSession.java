package com.mychat.imServer.server.session;

import com.mychat.entity.ImNode;
import com.mychat.imServer.distributed.PeerSender;
import com.mychat.imServer.distributed.WorkerRouter;
import com.mychat.imServer.server.session.entity.SessionCache;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
public class RemoteSession implements ServerSession, Serializable {
    private static final long serialVersionUID = -400010884211394846L;
    SessionCache cache;
    private boolean valid = true;
    public RemoteSession(SessionCache cache)
    {
        this.cache = cache;
    }

    @Override
    public void writeAndFlush(Object pkg) {
        ImNode imNode = cache.getImNode();
        long nodeId = imNode.getId();
        PeerSender sender = WorkerRouter.getInst().route(nodeId);
        if(null!=sender) {
            sender.writeAndFlush(pkg);
        }
    }

    @Override
    public String getSessionId() {
        return cache.getSessionId();
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getUserId() {
        return cache.getUserId();
    }
}