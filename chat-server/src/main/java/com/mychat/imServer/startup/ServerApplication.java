package com.mychat.imServer.startup;

import com.mychat.imServer.server.ChatServer;
import com.mychat.imServer.server.session.service.SessionManger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan("com.mychat")
@SpringBootApplication
public class ServerApplication {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(ServerApplication.class, args);
        SessionManger sessionManger = context.getBean(SessionManger.class);
        SessionManger.setSingleInstance(sessionManger);
        ChatServer nettyServer = context.getBean(ChatServer.class);
        nettyServer.run();
    }
}