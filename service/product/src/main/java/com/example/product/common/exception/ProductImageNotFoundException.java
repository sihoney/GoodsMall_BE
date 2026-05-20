package com.example.product.common.exception;

public class ProductImageNotFoundException extends CustomException {

    public ProductImageNotFoundException() {
        super(ErrorCode.PRODUCT_IMAGE_NOT_FOUND);
    }
}
