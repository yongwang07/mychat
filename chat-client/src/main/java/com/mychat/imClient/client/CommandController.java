package com.mychat.imClient.client;

import com.mychat.cocurrent.FutureTaskScheduler;
import com.mychat.entity.ImNode;
import com.mychat.entity.LoginBack;
import com.mychat.im.common.bean.UserDTO;
import com.mychat.imClient.ClientSender.ChatSender;
import com.mychat.imClient.ClientSender.LoginSender;
import com.mychat.imClient.clientCommand.BaseCommand;
import com.mychat.imClient.clientCommand.ChatConsoleCommand;
import com.mychat.imClient.clientCommand.ClientCommandMenu;
import com.mychat.imClient.clientCommand.LoginConsoleCommand;
import com.mychat.imClient.clientCommand.LogoutConsoleCommand;
import com.mychat.imClient.feignClient.WebOperator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoop;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Data
@Service("CommandController")
public class CommandController {
    private  int reConnectCount=0;

    @Autowired
    ChatConsoleCommand chatConsoleCommand;

    @Autowired
    LoginConsoleCommand loginConsoleCommand;

    @Autowired
    LogoutConsoleCommand logoutConsoleCommand;

    @Autowired
    ClientCommandMenu clientCommandMenu;

    private Map<String, BaseCommand> commandMap;

    private String menuString;

    private ClientSession session;

    @Autowired
    private NettyClient nettyClient;

    private Channel channel;

    @Autowired
    private ChatSender chatSender;

    @Autowired
    private LoginSender loginSender;

    private boolean connectFlag = false;
    private UserDTO user;

    GenericFutureListener<ChannelFuture> closeListener = (ChannelFuture f) -> {
        channel = f.channel();
        ClientSession session = channel.attr(ClientSession.SESSION_KEY).get();
        session.close();
        notifyCommandThread();
    };

    GenericFutureListener<ChannelFuture> connectedListener = (ChannelFuture f) -> {
        final EventLoop eventLoop = f.channel().eventLoop();
        if (!f.isSuccess() && ++reConnectCount<3){
            log.info(String.format("connect failed, reconnect %s after 10s"),reConnectCount);
            eventLoop.schedule(() -> nettyClient.doConnect(), 10, TimeUnit.SECONDS);
            connectFlag = false;
        } else if(f.isSuccess()) {
            connectFlag = true;
            log.info("connected !!!!");
            channel = f.channel();
            session = new ClientSession(channel);
            session.setConnected(true);
            channel.closeFuture().addListener(closeListener);
            notifyCommandThread();
        } else {
            connectFlag = false;
            notifyCommandThread();
        }
    };
    private Scanner scanner;

    public void initCommandMap() {
        commandMap = new HashMap<>();
        commandMap.put(clientCommandMenu.getKey(), clientCommandMenu);
        commandMap.put(chatConsoleCommand.getKey(), chatConsoleCommand);
        commandMap.put(loginConsoleCommand.getKey(), loginConsoleCommand);
        commandMap.put(logoutConsoleCommand.getKey(), logoutConsoleCommand);

        Set<Map.Entry<String, BaseCommand>> entrys = commandMap.entrySet();
        Iterator<Map.Entry<String, BaseCommand>> iterator = entrys.iterator();

        StringBuilder menus = new StringBuilder();
        menus.append("[menu] ");
        while (iterator.hasNext()) {
            BaseCommand next = iterator.next().getValue();
            menus.append(next.getKey())
                    .append("->")
                    .append(next.getTip())
                    .append(" | ");
        }
        menuString = menus.toString();
        clientCommandMenu.setAllCommandsShow(menuString);
    }

    public void startConnectServer() {
        FutureTaskScheduler.add(() -> {
            nettyClient.setConnectedListener(connectedListener);
            nettyClient.doConnect();
        });
    }
    public synchronized void notifyCommandThread() {
        this.notify();
    }

    public synchronized void waitCommandThread() {
        try {
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void userLoginAndConnectToServer() {
        if (isConnectFlag()) {
            return;
        }
        LoginConsoleCommand command = (LoginConsoleCommand) commandMap.get(LoginConsoleCommand.KEY);
        command.exec(scanner);
        UserDTO user = new UserDTO();
        user.setUserId(command.getUserName());
        user.setToken(command.getPassword());
        user.setDevId("12345");
        LoginBack webBack = WebOperator.login(command.getUserName(), command.getPassword());
        List<ImNode> nodeList = webBack.getImNodeList();
        ImNode bestNode = null;
        if (nodeList.size() > 0) {
            Collections.sort(nodeList);
        } else {
            log.error("unable to connect server");
        }
        nettyClient.setConnectedListener(connectedListener);
        for (int i = 0; i < nodeList.size(); i++) {
            bestNode = nodeList.get(i);
            nettyClient.setHost(bestNode.getHost());
            nettyClient.setPort(bestNode.getPort());
            nettyClient.doConnect();
            waitCommandThread();
            if (connectFlag) {
                break;
            }
            if (i == nodeList.size()) {
                return;
            }
        }
        this.user = user;
        session.setUser(user);
        loginSender.setUser(user);
        loginSender.setSession(session);
        loginSender.sendLoginMsg();
        waitCommandThread();
        connectFlag = true;
    }

    public void startCommandThread() throws InterruptedException {
        scanner = new Scanner(System.in);
        Thread.currentThread().setName("Demo command Thread");
        while (true) {
            while (connectFlag == false) {
                userLoginAndConnectToServer();
            }
            while (null != session) {
                ChatConsoleCommand command = (ChatConsoleCommand) commandMap.get(ChatConsoleCommand.KEY);
                command.exec(scanner);
                startOneChat(command);
            }
        }
    }

    private void startOneChat(ChatConsoleCommand c) {
        if (!isLogin()) {
            return;
        }
        chatSender.setSession(session);
        chatSender.setUser(user);
        chatSender.sendChatMsg(c.getToUserId(), c.getMessage());
    }

    private void startLogout(BaseCommand command) {
        if (!isLogin()) {
            return;
        }
    }

    public boolean isLogin() {
        if (null == session) {
            log.info("session is null");
            return false;
        }
        return session.isLogin();
    }
}