package com.mychat.service.impl;


import com.mychat.mybatis.entity.UserPO;
import com.mychat.mybatis.mapper.UserMapper;
import com.mychat.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public UserPO login(UserPO user) {
        //TODO
        return user;
    }

    @Cacheable(value = "mychat:User:", key = "#userid")
    public UserPO getById(String userid) {
        //TODO
        return null;
    }

    @CacheEvict(value = "mychat:User:", key = "#userid")
    public int deleteById(String userid) {
        //TODO
        return 0;
    }
}
