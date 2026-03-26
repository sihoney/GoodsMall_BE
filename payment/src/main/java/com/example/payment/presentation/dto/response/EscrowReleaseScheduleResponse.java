package com.example.payment.presentation.dto.response;

import com.example.payment.application.dto.EscrowReleaseScheduleResult;
import java.time.LocalDateTime;
import java.util.UUID;

public record EscrowReleaseScheduleResponse(
        UUID orderId,
        LocalDateTime deliveredAt,
        LocalDateTime releaseAt
) {

    public static EscrowReleaseScheduleResponse from(EscrowReleaseScheduleResult result) {
        return new EscrowReleaseScheduleResponse(
                result.orderId(),
                result.deliveredAt(),
                result.releaseAt()
        );
    }
}
