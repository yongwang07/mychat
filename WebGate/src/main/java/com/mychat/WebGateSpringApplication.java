package com.mychat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "com.mychat")
public class WebGateSpringApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebGateSpringApplication.class, args);
    }
}