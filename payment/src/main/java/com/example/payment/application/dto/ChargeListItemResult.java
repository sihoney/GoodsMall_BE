package com.example.payment.application.dto;

import com.example.payment.domain.enumtype.ChargeStatus;
import com.example.payment.domain.enumtype.PgProvider;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * charge 목록 조회에서 사용하는 단건 결과다.
 */
public record ChargeListItemResult(
        UUID chargeId,
        Long requestedAmount,
        Long approvedAmount,
        ChargeStatus chargeStatus,
        PgProvider pgProvider,
        LocalDateTime requestedAt,
        LocalDateTime approvedAt,
        LocalDateTime failedAt
) {
}
