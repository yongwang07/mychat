package com.mychat.service;

import com.mychat.mybatis.entity.UserPO;

public interface UserService {
    UserPO login(UserPO user);

    UserPO getById(String userid);

    int deleteById(String userid);
}