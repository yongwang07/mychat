package com.mychat.imClient.clientHandler;


import com.mychat.im.common.bean.msg.ProtoMsg;
import com.mychat.imClient.ClientSender.LoginSender;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.stereotype.Service;

@ChannelHandler.Sharable
@Service("ChatMsgHandler")
public class ChatMsgHandler extends ChannelInboundHandlerAdapter {
    private LoginSender sender;

    public ChatMsgHandler(LoginSender sender) {
        this.sender = sender;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (null == msg || !(msg instanceof ProtoMsg.Message)) {
            super.channelRead(ctx, msg);
            return;
        }
        ProtoMsg.Message pkg = (ProtoMsg.Message) msg;
        ProtoMsg.HeadType headType = pkg.getType();
        if (!headType.equals(ProtoMsg.HeadType.MESSAGE_REQUEST)) {
            super.channelRead(ctx, msg);
            return;
        }
        ProtoMsg.MessageRequest req = pkg.getMessageRequest();
        String content = req.getContent();
        String uid = req.getFrom();
        System.out.println("received from: " + uid + " -> " + content);
    }
}