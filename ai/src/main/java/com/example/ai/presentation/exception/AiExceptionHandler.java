package com.example.ai.presentation.exception;

import com.example.ai.common.exception.AiEmbeddingException;
import com.example.ai.common.exception.AiProductDraftAssistException;
import com.example.ai.presentation.dto.response.ApiErrorResponse;
import com.example.ai.presentation.dto.response.ApiResponse;
import com.todaylunch.common.security.exception.AuthorizationDeniedException;
import com.todaylunch.common.security.exception.InvalidTokenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AiExceptionHandler {

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidToken(InvalidTokenException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail("INVALID_TOKEN", e.getMessage()));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthorizationDenied(AuthorizationDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail("FORBIDDEN", e.getMessage()));
    }

    @ExceptionHandler(AiEmbeddingException.class)
    public ResponseEntity<ApiResponse<Object>> handleAiEmbeddingException(AiEmbeddingException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(ApiResponse.fail("AI_EMBEDDING_ERROR", e.getMessage()));
    }

    @ExceptionHandler(AiProductDraftAssistException.class)
    public ResponseEntity<ApiResponse<Object>> handleAiProductDraftAssistException(AiProductDraftAssistException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(ApiResponse.fail("AI_PRODUCT_DRAFT_ASSIST_ERROR", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("INVALID_INPUT_VALUE", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleUnexpected(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, ApiErrorResponse.of("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.")));
    }
}

