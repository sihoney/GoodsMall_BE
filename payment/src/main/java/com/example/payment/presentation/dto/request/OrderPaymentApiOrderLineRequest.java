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
        @NotNull(message = "주문 항목 ID는 필수입니다.")
        UUID orderItemId,

        @NotNull(message = "판매자 ID는 필수입니다.")
        UUID sellerId,

        @NotNull(message = "주문 시점 단가는 필수입니다.")
        @Positive(message = "주문 시점 단가는 0보다 커야 합니다.")
        BigDecimal unitPriceSnapshot,

        @NotNull(message = "수량은 필수입니다.")
        @Positive(message = "수량은 0보다 커야 합니다.")
        Integer quantity,

        @NotNull(message = "주문 항목 총액은 필수입니다.")
        @Positive(message = "주문 항목 총액은 0보다 커야 합니다.")
        BigDecimal lineTotalPrice
) {
}
