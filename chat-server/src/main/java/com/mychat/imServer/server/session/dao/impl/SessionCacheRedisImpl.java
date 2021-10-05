package com.mychat.imServer.server.session.dao.impl;


import com.mychat.imServer.server.session.dao.SessionCacheDAO;
import com.mychat.imServer.server.session.entity.SessionCache;
import com.mychat.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

public class SessionCacheRedisImpl implements SessionCacheDAO {

    public static final String REDIS_PREFIX = "SessionCache:id:";
    @Autowired
    protected StringRedisTemplate stringRedisTemplate;
    private static final long CACHE_LONG = 60 * 4;

    @Override
    public void save(final SessionCache sessionCache) {
        String key = REDIS_PREFIX + sessionCache.getSessionId();
        String value = JsonUtil.pojoToJson(sessionCache);
        stringRedisTemplate.opsForValue().set(key, value, CACHE_LONG, TimeUnit.MINUTES);
    }

    @Override
    public SessionCache get(final String sessionId) {
        String key = REDIS_PREFIX + sessionId;
        String value = (String) stringRedisTemplate.opsForValue().get(key);
        if (!StringUtils.isEmpty(value)) {
            return JsonUtil.jsonToPojo(value, SessionCache.class);
        }
        return null;
    }

  @Override
  public void remove( String sessionId) {
        String key = REDIS_PREFIX + sessionId;
        stringRedisTemplate.delete(key);
    }
}