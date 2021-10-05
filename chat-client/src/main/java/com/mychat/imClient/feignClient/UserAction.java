package com.mychat.imClient.feignClient;

import feign.Param;
import feign.RequestLine;


public interface UserAction {
    @RequestLine("GET /user/login/{username}/{password}")
    public String loginAction(
            @Param("username") String username,
            @Param("password") String password);

    @RequestLine("GET /{userid}")
    public String getById(@Param("userid") Integer userid);
}