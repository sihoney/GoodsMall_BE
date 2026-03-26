package com.example.product.presentation.exception;

import com.example.product.presentation.exception.dto.ErrorCode;

public class CategoryDepthExceededException extends CustomException {
    public CategoryDepthExceededException() {
        super(ErrorCode.CATEGORY_NOT_FOUND);
    }
}
