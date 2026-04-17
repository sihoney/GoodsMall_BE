package com.example.ai.common.exception;

public class ProductDraftAssistDuplicateFieldKeyException extends CustomException {

    public ProductDraftAssistDuplicateFieldKeyException() {
        super(ErrorCode.AI_ASSIST_DUPLICATE_FIELD_KEY);
    }
}
