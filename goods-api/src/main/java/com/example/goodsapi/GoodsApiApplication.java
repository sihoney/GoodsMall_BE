package com.example.goodsapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example")
public class GoodsApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoodsApiApplication.class, args);
    }
}