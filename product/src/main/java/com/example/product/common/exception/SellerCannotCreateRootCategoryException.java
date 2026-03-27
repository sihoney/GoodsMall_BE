package com.example.product.common.exception;

public class SellerCannotCreateRootCategoryException extends CustomException {
    public SellerCannotCreateRootCategoryException() {
        super(ErrorCode.SELLER_CANNOT_CREATE_ROOT_CATEGORY);
    }
}
