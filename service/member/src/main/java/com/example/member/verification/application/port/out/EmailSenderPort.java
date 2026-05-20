package com.example.member.verification.application.port.out;

public interface EmailSenderPort {

    void send(String to, String subject, String body, boolean html);
}
