package com.example.ai.common.exception;

public class ProductDraftAssistImageReadException extends AiProductDraftAssistException {

    public ProductDraftAssistImageReadException(String message, Throwable cause) {
        super(ErrorCode.AI_PRODUCT_DRAFT_ASSIST_IMAGE_READ_ERROR, message, cause);
    }
}
