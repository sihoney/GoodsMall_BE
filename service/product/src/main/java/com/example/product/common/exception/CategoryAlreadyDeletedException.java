package com.example.product.common.exception;

public class CategoryAlreadyDeletedException extends CustomException {
    public CategoryAlreadyDeletedException() {
        super(ErrorCode.CATEGORY_ALREADY_DELETED);
    }
}
