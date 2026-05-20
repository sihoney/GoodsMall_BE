package com.example.ai.common.exception;

public class AiProductDraftAssistException extends CustomException {

    public AiProductDraftAssistException() {
        super(ErrorCode.AI_PRODUCT_DRAFT_ASSIST_ERROR);
    }

    public AiProductDraftAssistException(Throwable cause) {
        super(ErrorCode.AI_PRODUCT_DRAFT_ASSIST_ERROR, cause);
    }

    public AiProductDraftAssistException(String message) {
        super(ErrorCode.AI_PRODUCT_DRAFT_ASSIST_ERROR, message);
    }

    public AiProductDraftAssistException(String message, Throwable cause) {
        super(ErrorCode.AI_PRODUCT_DRAFT_ASSIST_ERROR, message, cause);
    }

    protected AiProductDraftAssistException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    protected AiProductDraftAssistException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    protected AiProductDraftAssistException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
