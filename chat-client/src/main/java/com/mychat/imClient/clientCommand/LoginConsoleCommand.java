package com.mychat.imClient.clientCommand;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Scanner;

@Slf4j
@Data
@Service("LoginConsoleCommand")
public class LoginConsoleCommand implements BaseCommand {
    public static final String KEY = "1";

    private String userName;
    private String password;

    @Override
    public void exec(Scanner scanner) {
        System.out.println("login：username@password");
        String s = scanner.next();
        String[] array = s.split("@");
        userName = array[0];
        password = array[1];
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getTip() {
        return "Login";
    }
}