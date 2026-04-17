package com.example.ai.common.exception;

public class AiEmbeddingException extends CustomException {

    public AiEmbeddingException() {
        super(ErrorCode.AI_EMBEDDING_ERROR);
    }

    public AiEmbeddingException(Throwable cause) {
        super(ErrorCode.AI_EMBEDDING_ERROR, cause);
    }

    public AiEmbeddingException(String message) {
        super(ErrorCode.AI_EMBEDDING_ERROR, message);
    }

    public AiEmbeddingException(String message, Throwable cause) {
        super(ErrorCode.AI_EMBEDDING_ERROR, message, cause);
    }
}
