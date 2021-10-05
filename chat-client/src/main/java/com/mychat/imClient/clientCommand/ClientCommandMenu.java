package com.mychat.imClient.clientCommand;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.Scanner;

@Data
@Service("ClientCommandMenu")
public class ClientCommandMenu implements BaseCommand {

    public static final String KEY = "0";

    private String allCommandsShow;
    private String commandInput;

    @Override
    public void exec(Scanner scanner) {
        System.err.println(allCommandsShow);
        commandInput = scanner.next();
    }

    @Override
    public String getKey()
    {
        return KEY;
    }

    @Override
    public String getTip()
    {
        return "show all commands";
    }
}