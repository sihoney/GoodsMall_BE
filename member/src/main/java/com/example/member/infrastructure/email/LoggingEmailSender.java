package com.example.member.infrastructure.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingEmailSender implements EmailSender {

    @Override
    public void send(String to, String subject, String body) {
        log.info("Sending email. to={}, subject={}, body={}", to, subject, body);
    }
}
