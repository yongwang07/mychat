package com.mychat.imServer.server.session.service;

import com.mychat.entity.ImNode;
import com.mychat.im.common.bean.Notification;
import com.mychat.imServer.distributed.ImWorker;
import com.mychat.imServer.distributed.OnlineCounter;
import com.mychat.imServer.distributed.WorkerRouter;
import com.mychat.imServer.server.session.LocalSession;
import com.mychat.imServer.server.session.RemoteSession;
import com.mychat.imServer.server.session.ServerSession;
import com.mychat.imServer.server.session.dao.SessionCacheDAO;
import com.mychat.imServer.server.session.dao.UserCacheDAO;
import com.mychat.imServer.server.session.entity.SessionCache;
import com.mychat.imServer.server.session.entity.UserCache;
import com.mychat.util.JsonUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Data
@Repository("SessionManger")
public class SessionManger {
    @Autowired
    UserCacheDAO userCacheDAO;

    @Autowired
    SessionCacheDAO sessionCacheDAO;

    private static SessionManger singleInstance = null;

    private ConcurrentHashMap<String, ServerSession> sessionMap = new ConcurrentHashMap<>();

    public static SessionManger inst() {
        return singleInstance;
    }

    public static void setSingleInstance(SessionManger singleInstance) {
        SessionManger.singleInstance = singleInstance;
    }

    public void addLocalSession(LocalSession session) {
        String sessionId = session.getSessionId();
        sessionMap.put(sessionId, session);
        String uid = session.getUser().getUserId();
        ImNode node = ImWorker.getInst().getLocalNodeInfo();
        SessionCache sessionCache = new SessionCache(sessionId, uid, node);
        sessionCacheDAO.save(sessionCache);
        userCacheDAO.addSession(uid, sessionCache);
        OnlineCounter.getInst().increment();
        log.info("local session added：{},  total:{} ",
                JsonUtil.pojoToJson(session.getUser()),
                OnlineCounter.getInst().getCurValue());
        ImWorker.getInst().incBalance();
        notifyOtherImNodeOnLine(session);
    }

    public List<ServerSession> getSessionsBy(String userId) {
        UserCache user = userCacheDAO.get(userId);
        if (null == user) {
            log.info("user：{} off line? not found in cache ", userId);
            return null;
        }
        Map<String, SessionCache> allSession = user.getMap();
        if (null == allSession || allSession.size() == 0) {
            log.info("user：{} off line? no session ", userId);
            return null;
        }
        List<ServerSession> sessions = new LinkedList<>();
        allSession.values().stream().forEach(sessionCache -> {
            String sid = sessionCache.getSessionId();
            ServerSession session = sessionMap.get(sid);
            if (session == null) {
                session = new RemoteSession(sessionCache);
                sessionMap.put(sid, session);
            }
            sessions.add(session);
        });
        return sessions;
    }

    public void closeSession(ChannelHandlerContext ctx) {
        LocalSession session = ctx.channel().attr(LocalSession.SESSION_KEY).get();
        if (null == session || !session.isValid()) {
            log.error("session is null or isValid");
            return;
        }
        session.close();
        this.removeSession(session.getSessionId());
        notifyOtherImNodeOffLine(session);
    }

    private void notifyOtherImNodeOffLine(LocalSession session) {
        if (null == session || session.isValid()) {
            log.error("session is null or isValid");
            return;
        }
        int type = Notification.SESSION_OFF;
        Notification<Notification.ContentWrapper> notification = Notification.wrapContent(session.getSessionId());
        notification.setType(type);
        WorkerRouter.getInst().sendNotification(JsonUtil.pojoToJson(notification));
    }

    private void notifyOtherImNodeOnLine(LocalSession session) {
        int type = Notification.SESSION_ON;
        Notification<Notification.ContentWrapper> notification = Notification.wrapContent(session.getSessionId());
        notification.setType(type);
        WorkerRouter.getInst().sendNotification(JsonUtil.pojoToJson(notification));
    }

    public void removeSession(String sessionId) {
        if (!sessionMap.containsKey(sessionId)) return;
        ServerSession session = sessionMap.get(sessionId);
        String uid = session.getUserId();
        OnlineCounter.getInst().decrement();
        log.info("local session dec：{},  total online:{} ", uid,
                OnlineCounter.getInst().getCurValue());
        ImWorker.getInst().decrBalance();
        userCacheDAO.removeSession(uid, sessionId);
        sessionCacheDAO.remove(sessionId);
        sessionMap.remove(sessionId);
    }

    public void removeRemoteSession(String sessionId) {
        if (!sessionMap.containsKey(sessionId)) {
            return;
        }
        sessionMap.remove(sessionId);
    }
}