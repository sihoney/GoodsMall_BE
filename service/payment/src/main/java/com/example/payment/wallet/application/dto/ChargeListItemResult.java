package com.example.payment.wallet.application.dto;

import com.example.payment.wallet.domain.enumtype.ChargeStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * charge 紐⑸줉 議고쉶?먯꽌 ?ъ슜?섎뒗 ?④굔 寃곌낵??
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
