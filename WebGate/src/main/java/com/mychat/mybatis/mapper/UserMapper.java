package com.mychat.mybatis.mapper;

import com.mychat.mybatis.entity.UserPO;
import com.mychat.mybatis.utility.MyMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends MyMapper<UserPO>
{ }