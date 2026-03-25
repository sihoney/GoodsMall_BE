package com.example.product.presentation.exception;

import com.example.product.presentation.exception.dto.ErrorCode;

public class ProductNotFoundException extends CustomException {

    public ProductNotFoundException() {
        super(ErrorCode.PRODUCT_NOT_FOUND);
    }
}
