package com.example.cartservice.cart.presentation.exception;

public class CartItemDuplicateException extends CustomException {

    public CartItemDuplicateException() {
        super(ErrorCode.CART_ITEM_DUPLICATE);
    }
}
