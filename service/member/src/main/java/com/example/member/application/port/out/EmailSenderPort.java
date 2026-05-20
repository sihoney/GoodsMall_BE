package com.example.member.application.port.out;

public interface EmailSenderPort {

    void send(String to, String subject, String body, boolean html);
}
