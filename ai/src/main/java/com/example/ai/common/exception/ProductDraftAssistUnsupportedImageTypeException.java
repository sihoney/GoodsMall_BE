package com.example.ai.common.exception;

public class ProductDraftAssistUnsupportedImageTypeException extends CustomException {

    public ProductDraftAssistUnsupportedImageTypeException() {
        super(ErrorCode.AI_ASSIST_UNSUPPORTED_IMAGE_TYPE);
    }
}
