package com.example.ai.common.exception;

public class ProductDraftAssistResponseInvalidException extends AiProductDraftAssistException {

    public ProductDraftAssistResponseInvalidException(String message) {
        super(ErrorCode.AI_PRODUCT_DRAFT_ASSIST_RESPONSE_INVALID_ERROR, message);
    }

    public ProductDraftAssistResponseInvalidException(String message, Throwable cause) {
        super(ErrorCode.AI_PRODUCT_DRAFT_ASSIST_RESPONSE_INVALID_ERROR, message, cause);
    }
}
