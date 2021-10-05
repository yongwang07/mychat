package com.mychat.imClient.ClientSender;

import com.mychat.cocurrent.CallbackTask;
import com.mychat.cocurrent.CallbackTaskScheduler;
import com.mychat.im.common.bean.UserDTO;
import com.mychat.im.common.bean.msg.ProtoMsg;
import com.mychat.imClient.client.ClientSession;
import com.mychat.imClient.client.CommandController;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Data
@Slf4j
public abstract class BaseSender {
    private UserDTO user;
    private ClientSession session;

    @Autowired
    protected CommandController commandClient;

    public boolean isConnected() {
        if (null == session) {
            log.info("session is null");
            return false;
        }
        return session.isConnected();
    }

    public boolean isLogin() {
        if (null == session) {
            log.info("session is null");
            return false;
        }
        return session.isLogin();
    }

    public void sendMsg(ProtoMsg.Message message) {
        CallbackTaskScheduler.add(new CallbackTask<Boolean>() {
            @Override
            public Boolean execute() throws Exception {
                if (null == getSession()) {
                    throw new Exception("session is null");
                }
                if (!isConnected()) {
                    throw new Exception("connect failed");
                }
                final Boolean[] isSuccess = {false};
                ChannelFuture f = getSession().witeAndFlush(message);
                f.addListener((Future<? super Void> future) -> {
                        if (future.isSuccess()) {
                            isSuccess[0] = true;
                        }
                    }
                );
                try {
                    f.sync();
                } catch (InterruptedException e) {
                    isSuccess[0] = false;
                    e.printStackTrace();
                    throw new Exception("error occur");
                }
                return isSuccess[0];
            }

            @Override
            public void onBack(Boolean b) {
                if (b) {
                    BaseSender.this.sendSucceed(message);
                } else {
                    BaseSender.this.sendFailed(message);
                }
            }

            @Override
            public void onException(Throwable t) {
                BaseSender.this.sendException(message);
            }
        });
    }

    protected void sendSucceed(ProtoMsg.Message message) {
        log.info("send successfully");
    }

    protected void sendFailed(ProtoMsg.Message message) {
        log.info("send failed");
    }

    protected void sendException(ProtoMsg.Message message) {
        log.info("sent exception");
    }
}