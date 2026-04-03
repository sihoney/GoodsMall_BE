package com.example.payment.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * 주문 결제 API 요청의 주문 라인 DTO다.
 * Kafka 요청 계약과 최대한 유사하게 유지해 seller 집계 입력으로 사용한다.
 */
public record OrderPaymentApiOrderLineRequest(
        @NotNull(message = "orderItemId is required.")
        UUID orderItemId,

        @NotNull(message = "sellerId is required.")
        UUID sellerId,

        @NotNull(message = "unitPriceSnapshot is required.")
        @Positive(message = "unitPriceSnapshot must be positive.")
        BigDecimal unitPriceSnapshot,

        @NotNull(message = "quantity is required.")
        @Positive(message = "quantity must be positive.")
        Integer quantity,

        @NotNull(message = "lineTotalPrice is required.")
        @Positive(message = "lineTotalPrice must be positive.")
        BigDecimal lineTotalPrice
) {
}
