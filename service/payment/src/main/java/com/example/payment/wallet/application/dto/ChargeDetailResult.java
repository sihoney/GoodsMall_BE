package com.example.payment.wallet.application.dto;

import com.example.payment.wallet.domain.enumtype.ChargeStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * charge ?④굔 ?곸꽭 議고쉶 寃곌낵??
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
