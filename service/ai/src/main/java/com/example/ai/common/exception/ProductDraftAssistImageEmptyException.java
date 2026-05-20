package com.example.ai.common.exception;

public class ProductDraftAssistImageEmptyException extends CustomException {

    public ProductDraftAssistImageEmptyException() {
        super(ErrorCode.AI_ASSIST_IMAGE_EMPTY);
    }
}
