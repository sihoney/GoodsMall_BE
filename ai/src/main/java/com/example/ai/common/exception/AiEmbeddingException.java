package com.example.ai.common.exception;

public class AiEmbeddingException extends RuntimeException {

    public AiEmbeddingException(String message) {
        super(message);
    }

    public AiEmbeddingException(String message, Throwable cause) {
        super(message, cause);
    }
}
