package com.example.payment.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 주문 결제 API의 입력 DTO다.
 * 구매자 차감 금액과 seller 정산 예정 금액을 함께 전달한다.
 */
public record OrderPaymentRequest(
        @NotNull(message = "orderId is required.")
        UUID orderId,

        @NotNull(message = "buyerMemberId is required.")
        UUID buyerMemberId,

        @NotNull(message = "sellerMemberId is required.")
        UUID sellerMemberId,

        @NotNull(message = "orderAmount is required.")
        @Positive(message = "orderAmount must be positive.")
        Long orderAmount,

        @NotNull(message = "sellerReceivableAmount is required.")
        @Positive(message = "sellerReceivableAmount must be positive.")
        Long sellerReceivableAmount,

        LocalDateTime releaseAt
) {
}
