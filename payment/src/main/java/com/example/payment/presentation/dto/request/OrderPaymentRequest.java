package com.example.payment.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 주문 결제 API의 입력 DTO다.
 * 구매자 결제 정보와 seller 정산 정보를 함께 담는다.
 */
public record OrderPaymentRequest(
        @NotNull(message = "orderId is required.")
        UUID orderId,

        @NotNull(message = "buyerMemberId is required.")
        UUID buyerMemberId,

        @NotNull(message = "sellerMemberId is required.")
        UUID sellerMemberId,

        @NotNull(message = "orderAmount is required.")
        @DecimalMin(value = "0.01", message = "orderAmount must be positive.")
        @Digits(integer = 19, fraction = 2)
        BigDecimal orderAmount,

        @NotNull(message = "sellerReceivableAmount is required.")
        @DecimalMin(value = "0.01", message = "sellerReceivableAmount must be positive.")
        @Digits(integer = 19, fraction = 2)
        BigDecimal sellerReceivableAmount,

        LocalDateTime releaseAt
) {
}
