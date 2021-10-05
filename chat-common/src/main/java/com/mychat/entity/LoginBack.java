package com.mychat.entity;

import java.util.List;
import com.mychat.im.common.bean.UserDTO;
import lombok.Data;

@Data
public class LoginBack {
    List<ImNode> imNodeList;

    private String token;

    private UserDTO userDTO;
}