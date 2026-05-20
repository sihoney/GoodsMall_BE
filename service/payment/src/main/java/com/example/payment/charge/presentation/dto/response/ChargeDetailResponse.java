package com.example.payment.charge.presentation.dto.response;

import com.example.payment.charge.application.dto.ChargeDetailResult;
import com.example.payment.charge.domain.enumtype.ChargeStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * charge ?곸꽭 議고쉶 ?묐떟 DTO??
 */
public record ChargeDetailResponse(
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

    /**
     * application ?곸꽭 寃곌낵瑜?presentation ?묐떟?쇰줈 蹂?섑븳??
     */
    public static ChargeDetailResponse from(ChargeDetailResult result) {
        return new ChargeDetailResponse(
                result.chargeId(),
                result.memberId(),
                result.walletId(),
                result.requestedAmount(),
                result.approvedAmount(),
                result.tossBankCode(),
                result.pgOrderId(),
                result.pgPaymentKey(),
                result.chargeStatus(),
                result.requestedAt(),
                result.approvedAt(),
                result.failedAt(),
                result.failureReason()
        );
    }
}
