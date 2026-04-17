package com.example.ai.common.exception;

public class AiProductDraftAssistException extends RuntimeException {

    public AiProductDraftAssistException(String message) {
        super(message);
    }

    public AiProductDraftAssistException(String message, Throwable cause) {
        super(message, cause);
    }
}
