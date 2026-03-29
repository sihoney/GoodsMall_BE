package com.example.cartservice.wish.presentation.exception;

public class WishAlreadyExistsException extends CustomException {

    public WishAlreadyExistsException() {
        super(ErrorCode.WISH_ALREADY_EXISTS);
    }
}
