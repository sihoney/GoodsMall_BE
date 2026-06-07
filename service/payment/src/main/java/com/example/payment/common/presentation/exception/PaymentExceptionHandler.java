package com.example.payment.common.presentation.exception;

import com.example.payment.common.exception.CustomException;
import com.example.payment.common.exception.ErrorCode;
import com.example.payment.common.presentation.dto.response.ApiResponse;
import com.todaylunch.common.security.exception.InvalidTokenException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * payment API ?덉쇅瑜?怨듯넻 ?묐떟 ?뺤떇?쇰줈 蹂?섑븳??
 * 怨듯넻 ?덉쇅??ErrorCode瑜?洹몃?濡??ъ슜?섍퀬, ?몄쬆/?낅젰/?곹깭 媛?쒕뒗 蹂꾨룄 肄붾뱶濡?留ㅽ븨?쒕떎.
 */
@RestControllerAdvice
public class PaymentExceptionHandler {

    /**
     * ?몄쬆 ?ㅻ뜑 ?댁꽍 ?ㅽ뙣瑜?401 ?묐떟怨?{@code INVALID_TOKEN} ?ㅻ쪟 肄붾뱶濡?蹂?섑븳??
     */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidToken(InvalidTokenException exception) {
        // TODO: migrate common-security failures to SecurityException handler.
        return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail("INVALID_TOKEN", exception.getMessage()));
    }

    /**
     * payment 怨듯넻 ?덉쇅瑜?ErrorCode 湲곕컲 ?묐떟?쇰줈 蹂?섑븳??
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomException(CustomException e) {
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(ApiResponse.fail(e.getErrorCode().name(), e.getMessage()));
    }

    /**
     * domain/application??理쒖냼 guard ?덉쇅瑜?怨듯넻 API 肄붾뱶濡?蹂?섑븳??
     * IllegalArgumentException? ?낅젰 ?ㅻ쪟, IllegalStateException? ?곹깭 異⑸룎濡?援щ텇?쒕떎.
     */
    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class
    })
    public ResponseEntity<ApiResponse<Object>> handleRuntimeStateException(RuntimeException e) {
        ErrorCode errorCode = e instanceof IllegalStateException
                ? ErrorCode.INVALID_STATE
                : ErrorCode.INVALID_INPUT_VALUE;

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.fail(errorCode.name(), e.getMessage()));
    }

    /**
     * Bean Validation ?ㅽ뙣瑜?泥?踰덉㎏ ?꾨뱶 ?ㅻ쪟 湲곗??쇰줈 ?묐떟?쒕떎.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage() == null ? "?섎せ???붿껌?낅땲??" : error.getDefaultMessage())
                .orElse("?섎せ???붿껌?낅땲??");

        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(ErrorCode.INVALID_INPUT_VALUE.name(), message));
    }
}
