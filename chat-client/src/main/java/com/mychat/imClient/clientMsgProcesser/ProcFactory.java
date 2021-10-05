package com.mychat.imClient.clientMsgProcesser;


import com.mychat.im.common.bean.msg.ProtoMsg;

import java.util.HashMap;
import java.util.Map;

public class ProcFactory {
    private static ProcFactory instance;
    public static Map<ProtoMsg.HeadType, Proc> factory = new HashMap<>();

    static {
        instance = new ProcFactory();
    }

    public static ProcFactory getInstance()
    {
        return instance;
    }

    public Proc getOperation(ProtoMsg.HeadType type)
    {
        return factory.get(type);
    }
}