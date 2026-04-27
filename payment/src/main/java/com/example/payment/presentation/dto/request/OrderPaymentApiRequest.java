package com.example.payment.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 주문 결제 API 요청 DTO다.
 * 기존 Kafka OrderPaymentRequested 계약을 HTTP body 형태로 옮긴다.
 */
public record OrderPaymentApiRequest(

        @NotNull(message = "주문 ID는 필수입니다.")
        UUID orderId,

        @NotNull(message = "구매자 ID는 필수입니다.")
        UUID buyerId,

        @NotNull(message = "총 결제 금액은 필수입니다.")
        @Positive(message = "총 결제 금액은 0보다 커야 합니다.")
        BigDecimal totalPrice,

        @NotNull(message = "요청 시각은 필수입니다.")
        Instant requestedAt,

        @NotEmpty(message = "주문 라인은 비어 있을 수 없습니다.")
        List<@Valid OrderPaymentApiOrderLineRequest> orderLines
) {
}
