package com.example.cartservice.cart.presentation.exception;

public class CartNotFoundException extends CustomException {

    public CartNotFoundException() {
        super(ErrorCode.CART_NOT_FOUND);
    }
}
