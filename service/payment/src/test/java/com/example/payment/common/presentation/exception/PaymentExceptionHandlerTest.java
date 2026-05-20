package com.example.payment.common.presentation.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.payment.common.exception.CustomException;
import com.example.payment.common.exception.ErrorCode;
import com.example.payment.common.presentation.dto.response.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class PaymentExceptionHandlerTest {

    private final PaymentExceptionHandler paymentExceptionHandler = new PaymentExceptionHandler();

    @DisplayName("payment test")
    @Test
    void handleCustomExceptionReturnsApiResponse() {
        ResponseEntity<ApiResponse<Object>> response = paymentExceptionHandler.handleCustomException(
                new TestCustomException(ErrorCode.CHARGE_NOT_FOUND, "異⑹쟾 ?댁뿭???놁뒿?덈떎.")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().data()).isNull();
        assertThat(response.getBody().error()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("CHARGE_NOT_FOUND");
        assertThat(response.getBody().error().message()).isEqualTo("異⑹쟾 ?댁뿭???놁뒿?덈떎.");
    }

    @DisplayName("payment test")
    @Test
    void handleRuntimeStateExceptionReturnsApiResponse() {
        ResponseEntity<ApiResponse<Object>> response = paymentExceptionHandler.handleRuntimeStateException(
                new IllegalArgumentException("?섎せ???붿껌?낅땲??")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().data()).isNull();
        assertThat(response.getBody().error()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("INVALID_INPUT_VALUE");
        assertThat(response.getBody().error().message()).isEqualTo("?섎せ???붿껌?낅땲??");
    }

    private static final class TestCustomException extends CustomException {

        private TestCustomException(ErrorCode errorCode, String message) {
            super(errorCode, message);
        }
    }
}
