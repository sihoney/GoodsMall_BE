package com.example.product.common.exception;

public class CategoryHasChildrenException extends CustomException {
    public CategoryHasChildrenException() {
        super(ErrorCode.CATEGORY_HAS_CHILDREN);
    }
}
