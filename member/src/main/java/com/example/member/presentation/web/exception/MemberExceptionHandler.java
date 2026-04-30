package com.example.member.presentation.web.exception;

import com.example.member.common.exception.AccountVerificationAttemptLimitExceededException;
import com.example.member.common.exception.AccountVerificationNotAllowedException;
import com.example.member.common.exception.AccountVerificationNotFoundException;
import com.example.member.common.exception.AccountVerificationResendLimitExceededException;
import com.example.member.common.exception.DuplicateActiveRestrictionException;
import com.example.member.common.exception.DuplicateMemberEmailException;
import com.example.member.common.exception.DuplicateMemberReportException;
import com.example.member.common.exception.EmailSendFailedException;
import com.example.member.common.exception.EmailVerificationNotAllowedException;
import com.example.member.common.exception.EmailVerificationRequiredException;
import com.example.member.common.exception.ExpiredAccountVerificationException;
import com.example.member.common.exception.ExpiredEmailVerificationException;
import com.example.member.common.exception.InvalidAccountVerificationCodeException;
import com.example.member.common.exception.InvalidCurrentPasswordException;
import com.example.member.common.exception.InvalidEmailVerificationAutoLoginTokenException;
import com.example.member.common.exception.InvalidEmailVerificationTokenException;
import com.example.member.common.exception.InvalidLoginException;
import com.example.member.common.exception.InvalidPasswordResetTokenException;
import com.example.member.common.exception.LastLoginMethodRemovalNotAllowedException;
import com.example.member.common.exception.MemberNotFoundException;
import com.example.member.common.exception.MemberOauthAccountNotFoundException;
import com.example.member.common.exception.MemberReportNotFoundException;
import com.example.member.common.exception.MemberRestrictedException;
import com.example.member.common.exception.MemberRestrictionNotFoundException;
import com.example.member.common.exception.MemberWithdrawalException;
import com.example.member.common.exception.RefreshTokenNotFoundException;
import com.example.member.common.exception.SelfReportNotAllowedException;
import com.example.member.common.exception.SellerAlreadyRegisteredException;
import com.example.member.common.exception.SellerNotFoundException;
import com.example.member.presentation.web.dto.ApiResponse;
import com.todaylunch.common.security.exception.AuthorizationDeniedException;
import com.todaylunch.common.security.exception.InvalidTokenException;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    @ExceptionHandler(MemberOauthAccountNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleMemberOauthAccountNotFound(MemberOauthAccountNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail("MEMBER_OAUTH_ACCOUNT_NOT_FOUND", exception.getMessage()));
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

    @ExceptionHandler(InvalidCurrentPasswordException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidCurrentPassword(InvalidCurrentPasswordException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("INVALID_CURRENT_PASSWORD", exception.getMessage()));
    }

    @ExceptionHandler(InvalidPasswordResetTokenException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidPasswordResetToken(
            InvalidPasswordResetTokenException exception
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("INVALID_PASSWORD_RESET_TOKEN", exception.getMessage()));
    }

    @ExceptionHandler(EmailVerificationRequiredException.class)
    public ResponseEntity<ApiResponse<Object>> handleEmailVerificationRequired(
            EmailVerificationRequiredException exception
    ) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail("EMAIL_VERIFICATION_REQUIRED", exception.getMessage()));
    }

    @ExceptionHandler(InvalidEmailVerificationTokenException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidEmailVerificationToken(
            InvalidEmailVerificationTokenException exception
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("EMAIL_VERIFICATION_TOKEN_INVALID", exception.getMessage()));
    }

    @ExceptionHandler(InvalidEmailVerificationAutoLoginTokenException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidEmailVerificationAutoLoginToken(
            InvalidEmailVerificationAutoLoginTokenException exception
    ) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail("EMAIL_VERIFICATION_AUTO_LOGIN_TOKEN_INVALID", exception.getMessage()));
    }

    @ExceptionHandler(ExpiredEmailVerificationException.class)
    public ResponseEntity<ApiResponse<Object>> handleExpiredEmailVerification(
            ExpiredEmailVerificationException exception
    ) {
        return ResponseEntity.status(HttpStatus.GONE)
                .body(ApiResponse.fail("EMAIL_VERIFICATION_TOKEN_EXPIRED", exception.getMessage()));
    }

    @ExceptionHandler(EmailVerificationNotAllowedException.class)
    public ResponseEntity<ApiResponse<Object>> handleEmailVerificationNotAllowed(
            EmailVerificationNotAllowedException exception
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail("EMAIL_VERIFICATION_NOT_ALLOWED", exception.getMessage()));
    }

    @ExceptionHandler(EmailSendFailedException.class)
    public ResponseEntity<ApiResponse<Object>> handleEmailSendFailed(EmailSendFailedException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.fail("EMAIL_SEND_FAILED", exception.getMessage()));
    }

    @ExceptionHandler(MemberRestrictedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMemberRestricted(MemberRestrictedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail("MEMBER_RESTRICTED", exception.getMessage()));
    }

    @ExceptionHandler(MemberWithdrawalException.class)
    public ResponseEntity<ApiResponse<Object>> handleMemberWithdrawal(MemberWithdrawalException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(ApiResponse.fail(exception.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthorizationDenied(AuthorizationDeniedException exception) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail("ACCESS_DENIED", exception.getMessage()));
    }

    @ExceptionHandler(DuplicateActiveRestrictionException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateActiveRestriction(
            DuplicateActiveRestrictionException exception
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail("DUPLICATE_ACTIVE_RESTRICTION", exception.getMessage()));
    }

    @ExceptionHandler(DuplicateMemberReportException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateMemberReport(DuplicateMemberReportException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail("DUPLICATE_MEMBER_REPORT", exception.getMessage()));
    }

    @ExceptionHandler(SelfReportNotAllowedException.class)
    public ResponseEntity<ApiResponse<Object>> handleSelfReportNotAllowed(SelfReportNotAllowedException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("SELF_REPORT_NOT_ALLOWED", exception.getMessage()));
    }

    @ExceptionHandler(MemberReportNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleMemberReportNotFound(MemberReportNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail("MEMBER_REPORT_NOT_FOUND", exception.getMessage()));
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception
    ) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        if (message.isBlank()) {
            message = "요청 값이 올바르지 않습니다.";
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Object>> handleBindException(BindException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        if (message.isBlank()) {
            message = "요청 값이 올바르지 않습니다.";
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(
            ConstraintViolationException exception
    ) {
        String message = exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));

        if (message.isBlank()) {
            message = "요청 값이 올바르지 않습니다.";
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("BAD_REQUEST", exception.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalState(IllegalStateException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail("INVALID_STATE", exception.getMessage()));
    }

    @ExceptionHandler(LastLoginMethodRemovalNotAllowedException.class)
    public ResponseEntity<ApiResponse<Object>> handleLastLoginMethodRemovalNotAllowed(
            LastLoginMethodRemovalNotAllowedException exception
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail("LAST_LOGIN_METHOD", exception.getMessage()));
    }

    @ExceptionHandler(AccountVerificationNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccountVerificationNotFound(
            AccountVerificationNotFoundException exception
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail("ACCOUNT_VERIFICATION_NOT_FOUND", exception.getMessage()));
    }

    @ExceptionHandler(InvalidAccountVerificationCodeException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidAccountVerificationCode(
            InvalidAccountVerificationCodeException exception
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("ACCOUNT_VERIFICATION_CODE_INVALID", exception.getMessage()));
    }

    @ExceptionHandler(ExpiredAccountVerificationException.class)
    public ResponseEntity<ApiResponse<Object>> handleExpiredAccountVerification(
            ExpiredAccountVerificationException exception
    ) {
        return ResponseEntity.status(HttpStatus.GONE)
                .body(ApiResponse.fail("ACCOUNT_VERIFICATION_EXPIRED", exception.getMessage()));
    }

    @ExceptionHandler(AccountVerificationAttemptLimitExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccountVerificationAttemptLimitExceeded(
            AccountVerificationAttemptLimitExceededException exception
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail("ACCOUNT_VERIFICATION_ATTEMPT_LIMIT_EXCEEDED", exception.getMessage()));
    }

    @ExceptionHandler(AccountVerificationResendLimitExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccountVerificationResendLimitExceeded(
            AccountVerificationResendLimitExceededException exception
    ) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponse.fail("ACCOUNT_VERIFICATION_RESEND_LIMIT_EXCEEDED", exception.getMessage()));
    }

    @ExceptionHandler(AccountVerificationNotAllowedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccountVerificationNotAllowed(
            AccountVerificationNotAllowedException exception
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail("ACCOUNT_VERIFICATION_NOT_ALLOWED", exception.getMessage()));
    }
}
