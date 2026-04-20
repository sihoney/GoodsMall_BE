package com.example.ai.common.exception;

public class ProductDraftAssistImageCountExceededException extends CustomException {

    public ProductDraftAssistImageCountExceededException() {
        super(ErrorCode.AI_ASSIST_IMAGE_COUNT_EXCEEDED);
    }
}
