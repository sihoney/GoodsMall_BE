package com.example.member.presentation.exception;

import com.example.member.common.exception.AdminAccessDeniedException;
import com.example.member.common.exception.DuplicateActiveRestrictionException;
import com.example.member.common.exception.DuplicateMemberEmailException;
import com.example.member.common.exception.InvalidLoginException;
import com.example.member.common.exception.MemberNotFoundException;
import com.example.member.common.exception.MemberRestrictedException;
import com.example.member.common.exception.MemberRestrictionNotFoundException;
import com.example.member.common.exception.RefreshTokenNotFoundException;
import com.example.member.common.exception.SellerAlreadyRegisteredException;
import com.example.member.common.exception.SellerNotFoundException;
import com.example.member.presentation.dto.ApiResponse;
import com.todaylunch.common.security.exception.InvalidTokenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MemberExceptionHandler {

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleMemberNotFound(MemberNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail("MEMBER_NOT_FOUND", exception.getMessage()));
    }

    @ExceptionHandler(DuplicateMemberEmailException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateEmail(DuplicateMemberEmailException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail("DUPLICATE_MEMBER_EMAIL", exception.getMessage()));
    }

    @ExceptionHandler(SellerAlreadyRegisteredException.class)
    public ResponseEntity<ApiResponse<Object>> handleSellerAlreadyRegistered(
            SellerAlreadyRegisteredException exception
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail("SELLER_ALREADY_REGISTERED", exception.getMessage()));
    }

    @ExceptionHandler(SellerNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleSellerNotFound(SellerNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail("SELLER_NOT_FOUND", exception.getMessage()));
    }

    @ExceptionHandler(InvalidLoginException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidLogin(InvalidLoginException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail("INVALID_LOGIN", exception.getMessage()));
    }

    @ExceptionHandler(MemberRestrictedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMemberRestricted(MemberRestrictedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail("MEMBER_RESTRICTED", exception.getMessage()));
    }

    @ExceptionHandler(AdminAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAdminAccessDenied(AdminAccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail("ADMIN_ACCESS_DENIED", exception.getMessage()));
    }

    @ExceptionHandler(DuplicateActiveRestrictionException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateActiveRestriction(
            DuplicateActiveRestrictionException exception
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail("DUPLICATE_ACTIVE_RESTRICTION", exception.getMessage()));
    }

    @ExceptionHandler(MemberRestrictionNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleMemberRestrictionNotFound(
            MemberRestrictionNotFoundException exception
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail("MEMBER_RESTRICTION_NOT_FOUND", exception.getMessage()));
    }

    @ExceptionHandler({InvalidTokenException.class, RefreshTokenNotFoundException.class})
    public ResponseEntity<ApiResponse<Object>> handleInvalidToken(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail("INVALID_TOKEN", exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("BAD_REQUEST", exception.getMessage()));
    }
}
