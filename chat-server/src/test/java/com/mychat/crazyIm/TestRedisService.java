package com.mychat.crazyIm;

import com.mychat.imServer.server.session.dao.SessionCacheDAO;
import com.mychat.imServer.server.session.dao.UserCacheDAO;
import com.mychat.imServer.server.session.entity.SessionCache;
import com.mychat.imServer.server.session.entity.UserCache;
import com.mychat.imServer.startup.ServerApplication;
import com.mychat.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServerApplication.class)
@Slf4j
public class TestRedisService {
    @Autowired
    private UserCacheDAO userCacheDAO;
    @Autowired
    private SessionCacheDAO sessionCacheDAO;

    @Test
    public void testSaveSession() throws Exception {
        SessionCache cache=new SessionCache();
        cache.setSessionId("1");
        sessionCacheDAO.save(cache);
        SessionCache sessionCache2=  sessionCacheDAO.get("1");
        System.out.println("sessionCache2 = " + sessionCache2);
    }

    @Test
    public void testSaveUser() throws Exception {
        UserCache cache=new UserCache("2");
        SessionCache sessionCache=new SessionCache();
        sessionCache.setSessionId("1");
        cache.addSession(sessionCache);
        userCacheDAO.save(cache);
        UserCache userCache=  userCacheDAO.get("2");
        System.out.println("userCache = " + JsonUtil.pojoToJson(userCache));
    }
}