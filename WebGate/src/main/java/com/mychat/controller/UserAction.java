package com.mychat.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mychat.Balance.ImLoadBalance;
import com.mychat.controller.utility.BaseController;
import com.mychat.entity.ImNode;
import com.mychat.entity.LoginBack;
import com.mychat.im.common.bean.UserDTO;
import com.mychat.mybatis.entity.UserPO;
import com.mychat.service.UserService;
import com.mychat.util.JsonUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(value = "/user", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class UserAction extends BaseController {
    @Resource
    private UserService userService;
    @Resource
    private ImLoadBalance imLoadBalance;

    @ApiOperation(value = "login", notes = "login with user info")
    @RequestMapping(value = "/login/{username}/{password}", method = RequestMethod.GET)
    public String loginAction(
            @PathVariable("username") String username,
            @PathVariable("password") String password) {
        UserPO user = new UserPO();
        user.setUserName(username);
        user.setPassWord(password);
        user.setUserId(user.getUserName());
        LoginBack back = new LoginBack();
        List<ImNode> allWorker = imLoadBalance.getWorkers();
        back.setImNodeList(allWorker);
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        back.setUserDTO(userDTO);
        back.setToken(user.getUserId().toString());
        return JsonUtil.pojoToJson(back);
    }

    @ApiOperation(value = "delete node in zk", notes = "delete all node in zk")
    @RequestMapping(value = "/removeWorkers", method = RequestMethod.GET)
    public String removeWorkers() {
        imLoadBalance.removeWorkers();
        return "deleted!!!!";
    }
}