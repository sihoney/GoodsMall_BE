package com.example.member.common.exception;

public class EmailSendFailedException extends RuntimeException {

    public EmailSendFailedException() {
        super("Failed to send email.");
    }

    public EmailSendFailedException(String message) {
        super(message);
    }

    public EmailSendFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
