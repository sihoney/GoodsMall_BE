package com.example.member.verification.exception;

public class EmailSendFailedException extends RuntimeException {

    public EmailSendFailedException() {
        super("이메일 전송에 실패했습니다.");
    }

    public EmailSendFailedException(String message) {
        super(message);
    }

    public EmailSendFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
