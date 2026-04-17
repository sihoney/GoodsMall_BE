package com.example.ai.common.exception;

public class ProductDraftAssistFingerprintException extends AiProductDraftAssistException {

    public ProductDraftAssistFingerprintException(String message, Throwable cause) {
        super(ErrorCode.AI_PRODUCT_DRAFT_ASSIST_FINGERPRINT_ERROR, message, cause);
    }
}
