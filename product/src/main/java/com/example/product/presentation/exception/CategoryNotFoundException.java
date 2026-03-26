package com.example.product.presentation.exception;

import com.example.product.presentation.exception.dto.ErrorCode;

public class CategoryNotFoundException extends CustomException {
    public CategoryNotFoundException() {
        super(ErrorCode.CATEGORY_NOT_FOUND);
    }
}
