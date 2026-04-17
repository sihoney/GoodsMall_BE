package com.example.ai.common.exception;

public class ProductDraftAssistExternalCallException extends AiProductDraftAssistException {

    public ProductDraftAssistExternalCallException(String message, Throwable cause) {
        super(ErrorCode.AI_PRODUCT_DRAFT_ASSIST_EXTERNAL_CALL_ERROR, message, cause);
    }
}
