package com.example.product.common.exception;

public class ImageNotOwnedByProductException extends CustomException {

    public ImageNotOwnedByProductException() {
        super(ErrorCode.IMAGE_NOT_OWNED_BY_PRODUCT);
    }
}
