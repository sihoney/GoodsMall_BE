package com.example.ai.presentation.exception;

import com.example.ai.common.exception.CustomException;
import com.example.ai.common.exception.ErrorCode;
import com.example.ai.presentation.dto.response.ApiErrorResponse;
import com.example.ai.presentation.dto.response.ApiResponse;
import com.todaylunch.common.security.exception.AuthorizationDeniedException;
import com.todaylunch.common.security.exception.InvalidTokenException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class AiExceptionHandler {

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidToken(InvalidTokenException e) {
        // TODO: migrate common-security failures to SecurityException handler.
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail("INVALID_TOKEN", e.getMessage()));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthorizationDenied(AuthorizationDeniedException e) {
        // TODO: migrate common-security failures to SecurityException handler.
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail("FORBIDDEN", e.getMessage()));
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomException(CustomException e, HttpServletRequest request) {
        log.warn("AI custom exception. path={}, code={}, message={}",
                request.getRequestURI(),
                e.getErrorCode().name(),
                e.getMessage());
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(ApiResponse.fail(e.getErrorCode().name(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        FieldError firstFieldError = exception.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String message = firstFieldError == null
                ? "요청 값 검증에 실패했습니다."
                : "%s: %s".formatted(firstFieldError.getField(), firstFieldError.getDefaultMessage());

        log.warn("AI validation exception. path={}, message={}", request.getRequestURI(), message);
        return ResponseEntity.status(ErrorCode.AI_AUCTION_PRICE_RECOMMENDATION_REQUEST_INVALID.getHttpStatus())
                .body(ApiResponse.fail(ErrorCode.AI_AUCTION_PRICE_RECOMMENDATION_REQUEST_INVALID.name(), message));
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            ConstraintViolationException.class,
            HttpMediaTypeNotSupportedException.class
    })
    public ResponseEntity<ApiResponse<Object>> handleRequestBindingException(
            Exception exception,
            HttpServletRequest request
    ) {
        log.warn("AI request binding exception. path={}, message={}", request.getRequestURI(), exception.getMessage());
        return ResponseEntity.status(ErrorCode.AI_AUCTION_PRICE_RECOMMENDATION_REQUEST_INVALID.getHttpStatus())
                .body(ApiResponse.fail(
                        ErrorCode.AI_AUCTION_PRICE_RECOMMENDATION_REQUEST_INVALID.name(),
                        "요청 본문 형식이 올바르지 않습니다."
                ));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException exception,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = resolveUploadSizeErrorCode(exception);
        log.warn("AI multipart size exception. path={}, code={}, message={}",
                request.getRequestURI(), errorCode.name(), exception.getMessage());
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.fail(errorCode.name(), errorCode.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        return handleInvalidInputException(e, request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalState(IllegalStateException e, HttpServletRequest request) {
        return handleInvalidInputException(e, request);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<Object>> handleNullPointer(NullPointerException e, HttpServletRequest request) {
        return handleInvalidInputException(e, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleUnexpected(Exception e, HttpServletRequest request) {
        log.error("Unexpected AI exception. path={}", request.getRequestURI(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, ApiErrorResponse.of("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.")));
    }

    private ErrorCode resolveUploadSizeErrorCode(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            String className = current.getClass().getSimpleName();
            if ("FileSizeLimitExceededException".equals(className)) {
                return ErrorCode.AI_ASSIST_IMAGE_TOO_LARGE;
            }
            if ("SizeLimitExceededException".equals(className)) {
                return ErrorCode.AI_ASSIST_IMAGE_REQUEST_TOO_LARGE;
            }
            current = current.getCause();
        }
        return ErrorCode.AI_ASSIST_IMAGE_REQUEST_TOO_LARGE;
    }

    private ResponseEntity<ApiResponse<Object>> handleInvalidInputException(
            RuntimeException e,
            HttpServletRequest request
    ) {
        log.warn("AI invalid input exception. path={}, message={}", request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.getHttpStatus())
                .body(ApiResponse.fail(ErrorCode.INVALID_INPUT_VALUE.name(), e.getMessage()));
    }
}
