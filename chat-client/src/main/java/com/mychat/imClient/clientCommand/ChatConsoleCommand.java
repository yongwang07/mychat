package com.mychat.imClient.clientCommand;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Scanner;

@Slf4j
@Data
@Service("ChatConsoleCommand")
public class ChatConsoleCommand implements BaseCommand {

    private String toUserId;
    private String message;
    public static final String KEY = "2";

    @Override
    public void exec(Scanner scanner) {
        System.out.println("chat message: content@usename ");
        String s = scanner.next();
        String[] array = s.split("@");
        message = array[0];
        toUserId = array[1];
    }

    @Override
    public String getKey()
    {
        return KEY;
    }

    @Override
    public String getTip()
    {
        return "chat";
    }

}
