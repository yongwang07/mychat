package com.mychat.imServer.serverProcesser;


import com.mychat.im.common.bean.msg.ProtoMsg;
import com.mychat.imServer.server.session.LocalSession;

public interface ServerProcessor {
    ProtoMsg.HeadType type();
    boolean action(LocalSession ch, ProtoMsg.Message proto);
}