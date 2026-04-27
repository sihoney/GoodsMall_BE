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
        @NotNull(message = "주문 ID는 필수입니다.")
        UUID orderId,

        @NotNull(message = "구매자 회원 ID는 필수입니다.")
        UUID buyerMemberId,

        @NotNull(message = "판매자 회원 ID는 필수입니다.")
        UUID sellerMemberId,

        @NotNull(message = "주문 금액은 필수입니다.")
        @DecimalMin(value = "0.01", message = "주문 금액은 0보다 커야 합니다.")
        @Digits(integer = 19, fraction = 2)
        BigDecimal orderAmount,

        @NotNull(message = "판매자 정산 가능 금액은 필수입니다.")
        @DecimalMin(value = "0.01", message = "판매자 정산 가능 금액은 0보다 커야 합니다.")
        @Digits(integer = 19, fraction = 2)
        BigDecimal sellerReceivableAmount,

        LocalDateTime releaseAt
) {
}
