package com.example.settlement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
/**
 * settlement 모듈 실행 진입점이다.
 */
public class SettlementApplication {

    /**
     * settlement 애플리케이션을 실행한다.
     */
    public static void main(String[] args) {
        SpringApplication.run(SettlementApplication.class, args);
    }
}
