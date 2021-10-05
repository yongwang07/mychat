package com.mychat.imClient.starter;

import com.mychat.imClient.client.CommandController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@SpringBootApplication
public class ClientApplication {
    public static void main(String[] args) {
        ApplicationContext context =
                SpringApplication.run(ClientApplication.class, args);
        CommandController commandClient =
                context.getBean(CommandController.class);
        commandClient.initCommandMap();
        try {
            commandClient.startCommandThread();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}