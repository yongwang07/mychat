package com.mychat.imClient.clientMsgProcesser;


import com.mychat.im.common.bean.msg.ProtoMsg;
import com.mychat.imClient.client.ClientSession;

public interface Proc {
    ProtoMsg.HeadType op();
    void action(ClientSession ch, ProtoMsg.Message proto) throws Exception;
}