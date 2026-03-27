package com.example.payment.presentation.dto.response;

import com.example.payment.application.dto.ChargeListItemResult;
import com.example.payment.domain.enumtype.ChargeStatus;
import com.example.payment.domain.enumtype.PgProvider;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChargeListItemResponse(
        UUID chargeId,
        Long requestedAmount,
        Long approvedAmount,
        ChargeStatus chargeStatus,
        PgProvider pgProvider,
        LocalDateTime requestedAt,
        LocalDateTime approvedAt,
        LocalDateTime failedAt
) {

    public static ChargeListItemResponse from(ChargeListItemResult result) {
        return new ChargeListItemResponse(
                result.chargeId(),
                result.requestedAmount(),
                result.approvedAmount(),
                result.chargeStatus(),
                result.pgProvider(),
                result.requestedAt(),
                result.approvedAt(),
                result.failedAt()
        );
    }
}
