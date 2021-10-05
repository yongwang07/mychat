package com.mychat.imServer.serverHandler;


import com.mychat.im.common.bean.msg.ProtoMsg;
import com.mychat.imServer.distributed.ImWorker;
import com.mychat.util.JsonUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
@ChannelHandler.Sharable
public class ImNodeHeartBeatClientHandler extends ChannelInboundHandlerAdapter {
    String from = null;
    int seq = 0;
    private static final int HEARTBEAT_INTERVAL = 50;

    public ProtoMsg.Message buildMessageHeartBeat() {
        if (null == from) {
            from = JsonUtil.pojoToJson(ImWorker.getInst().getLocalNode());
        }
        ProtoMsg.Message.Builder mb = ProtoMsg.Message.newBuilder()
                .setType(ProtoMsg.HeadType.HEART_BEAT)
                .setSequence(++seq);
        ProtoMsg.MessageHeartBeat.Builder heartBeat =
                ProtoMsg.MessageHeartBeat.newBuilder()
                        .setSeq(seq)
                        .setJson(from)
                        .setUid("-1");
        mb.setHeartBeat(heartBeat.build());
        return mb.build();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx)
            throws Exception {
        heartBeat(ctx);
    }

    public void heartBeat(ChannelHandlerContext ctx) {
        ProtoMsg.Message message = buildMessageHeartBeat();
        ctx.executor().schedule(() -> {
            if (ctx.channel().isActive()) {
                ctx.writeAndFlush(message);
                heartBeat(ctx);
            }
        }, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (null == msg || !(msg instanceof ProtoMsg.Message)) {
            super.channelRead(ctx, msg);
            return;
        }
        ProtoMsg.Message pkg = (ProtoMsg.Message) msg;
        ProtoMsg.HeadType headType = pkg.getType();
        if (headType.equals(ProtoMsg.HeadType.HEART_BEAT)) {
            ProtoMsg.MessageHeartBeat messageHeartBeat = pkg.getHeartBeat();
            log.info(" received imNode HEART_BEAT msg from: " + messageHeartBeat.getJson());
            log.info(" received imNode HEART_BEAT seq: " + messageHeartBeat.getSeq());
            return;
        } else {
            super.channelRead(ctx, msg);
        }
    }
}