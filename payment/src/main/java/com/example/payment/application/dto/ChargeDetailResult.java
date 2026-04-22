package com.example.payment.application.dto;

import com.example.payment.domain.enumtype.ChargeStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * charge 단건 상세 조회 결과다.
 */
public record ChargeDetailResult(
        UUID chargeId,
        UUID memberId,
        UUID walletId,
        BigDecimal requestedAmount,
        BigDecimal approvedAmount,
        String tossBankCode,
        String pgOrderId,
        String pgPaymentKey,
        ChargeStatus chargeStatus,
        LocalDateTime requestedAt,
        LocalDateTime approvedAt,
        LocalDateTime failedAt,
        String failureReason
) {
}
