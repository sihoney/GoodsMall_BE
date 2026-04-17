package com.example.ai.common.exception;

public class ProductDraftAssistImageRequiredException extends CustomException {

    public ProductDraftAssistImageRequiredException() {
        super(ErrorCode.AI_ASSIST_IMAGE_REQUIRED);
    }
}
