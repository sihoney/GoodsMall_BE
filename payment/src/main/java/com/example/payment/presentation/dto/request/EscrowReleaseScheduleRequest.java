package com.example.payment.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 배송완료 기준 자동 구매확정 예약 API의 입력 DTO다.
 */
public record EscrowReleaseScheduleRequest(
        @NotNull(message = "deliveredAt is required.")
        LocalDateTime deliveredAt
) {
}
