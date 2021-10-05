package com.mychat.imClient.clientHandler;

import com.mychat.im.common.exception.BusinessException;
import com.mychat.im.common.exception.InvalidFrameException;
import com.mychat.imClient.client.CommandController;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@ChannelHandler.Sharable
@Service("ExceptionHandler")
public class ExceptionHandler extends ChannelInboundHandlerAdapter {

    @Autowired
    private CommandController commandController;

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof BusinessException) {
        } else if (cause instanceof InvalidFrameException) {
            log.error(cause.getMessage());
        } else {
            log.error(cause.getMessage());
            ctx.close();
            commandController.setConnectFlag(false);
            commandController.startConnectServer();
        }
    }

    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}