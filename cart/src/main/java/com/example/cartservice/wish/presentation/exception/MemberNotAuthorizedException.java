package com.example.cartservice.wish.presentation.exception;

public class MemberNotAuthorizedException extends CustomException {

    public MemberNotAuthorizedException() {
        super(ErrorCode.MEMBER_NOT_AUTHORIZED);
    }
}
