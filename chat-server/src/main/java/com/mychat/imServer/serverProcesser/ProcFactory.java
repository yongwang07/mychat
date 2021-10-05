package com.mychat.imServer.serverProcesser;


import com.mychat.im.common.bean.msg.ProtoMsg;

import java.util.HashMap;
import java.util.Map;

public class ProcFactory {
    private static ProcFactory instance;
    public static Map<ProtoMsg.HeadType, ServerReceiver> factory = new HashMap<>();

    static {
        instance = new ProcFactory();
    }

    private ProcFactory() {
        try {
            ServerReceiver proc = new LoginProcessor();
            factory.put(proc.op(), proc);
            proc = new ChatRedirectProcessor();
            factory.put(proc.op(), proc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static ProcFactory getInstance() {
        return instance;
    }

    public ServerReceiver getOperation(ProtoMsg.HeadType type) {
        return factory.get(type);
    }
}