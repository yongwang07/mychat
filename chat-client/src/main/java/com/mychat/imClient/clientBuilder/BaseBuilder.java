package com.mychat.imClient.clientBuilder;

import com.mychat.im.common.bean.msg.ProtoMsg;
import com.mychat.imClient.client.ClientSession;

public class BaseBuilder {
    protected ProtoMsg.HeadType type;
    private long seqId;
    private ClientSession session;

    public BaseBuilder(
            ProtoMsg.HeadType type,
            ClientSession session) {
        this.type = type;
        this.session = session;
    }

    public ProtoMsg.Message buildCommon(long seqId) {
        this.seqId = seqId;
        ProtoMsg.Message.Builder mb =
                ProtoMsg.Message
                        .newBuilder()
                        .setType(type)
                        .setSessionId(session.getSessionId())
                        .setSequence(seqId);
        return mb.buildPartial();
    }
}