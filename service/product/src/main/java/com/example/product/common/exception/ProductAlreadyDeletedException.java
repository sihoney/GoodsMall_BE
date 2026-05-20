package com.example.product.common.exception;

public class ProductAlreadyDeletedException extends CustomException {
    public ProductAlreadyDeletedException() {
        super(ErrorCode.PRODUCT_ALREADY_DELETED);
    }
}