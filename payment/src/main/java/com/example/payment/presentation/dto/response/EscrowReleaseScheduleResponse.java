package com.example.payment.presentation.dto.response;

import com.example.payment.application.dto.EscrowReleaseScheduleResult;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 자동 구매확정 예약 API의 응답 DTO다.
 */
public record EscrowReleaseScheduleResponse(
        UUID orderId,
        LocalDateTime deliveredAt,
        LocalDateTime releaseAt
) {

    /**
     * application 결과를 presentation 응답 형식으로 변환한다.
     */
    public static EscrowReleaseScheduleResponse from(EscrowReleaseScheduleResult result) {
        return new EscrowReleaseScheduleResponse(
                result.orderId(),
                result.deliveredAt(),
                result.releaseAt()
        );
    }
}
