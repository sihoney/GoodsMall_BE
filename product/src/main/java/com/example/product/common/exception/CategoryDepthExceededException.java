package com.example.product.common.exception;

public class CategoryDepthExceededException extends CustomException {
    public CategoryDepthExceededException() {
        super(ErrorCode.CATEGORY_NOT_FOUND);
    }
}
