package com.example.payment.application.dto;

import com.example.payment.domain.enumtype.ChargeStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * charge 목록 조회에서 사용하는 단건 결과다.
 */
public record ChargeListItemResult(
        UUID chargeId,
        BigDecimal requestedAmount,
        BigDecimal approvedAmount,
        ChargeStatus chargeStatus,
        String tossBankCode,
        LocalDateTime requestedAt,
        LocalDateTime approvedAt,
        LocalDateTime failedAt
) {
}
