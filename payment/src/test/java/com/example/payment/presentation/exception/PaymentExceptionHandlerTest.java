package com.example.payment.presentation.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.payment.common.exception.CustomException;
import com.example.payment.common.exception.ErrorCode;
import com.example.payment.presentation.dto.response.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class PaymentExceptionHandlerTest {

    private final PaymentExceptionHandler paymentExceptionHandler = new PaymentExceptionHandler();

    @DisplayName("CustomException은 공통 오류 응답 래퍼로 변환된다")
    @Test
    void handleCustomExceptionReturnsApiResponse() {
        ResponseEntity<ApiResponse<Object>> response = paymentExceptionHandler.handleCustomException(
                new TestCustomException(ErrorCode.CHARGE_NOT_FOUND, "충전 내역이 없습니다.")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().data()).isNull();
        assertThat(response.getBody().error()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("CHARGE_NOT_FOUND");
        assertThat(response.getBody().error().message()).isEqualTo("충전 내역이 없습니다.");
    }

    @DisplayName("IllegalArgumentException은 공통 오류 응답 래퍼로 변환된다")
    @Test
    void handleRuntimeStateExceptionReturnsApiResponse() {
        ResponseEntity<ApiResponse<Object>> response = paymentExceptionHandler.handleRuntimeStateException(
                new IllegalArgumentException("잘못된 요청입니다.")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().data()).isNull();
        assertThat(response.getBody().error()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("INVALID_INPUT_VALUE");
        assertThat(response.getBody().error().message()).isEqualTo("잘못된 요청입니다.");
    }

    private static final class TestCustomException extends CustomException {

        private TestCustomException(ErrorCode errorCode, String message) {
            super(errorCode, message);
        }
    }
}
