package com.example.payment.charge.presentation.dto.response;

import com.example.payment.charge.application.dto.ChargeListItemResult;
import com.example.payment.charge.domain.enumtype.ChargeStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * charge 紐⑸줉 ?④굔 ?묐떟 DTO??
 */
public record ChargeListItemResponse(
        UUID chargeId,
        BigDecimal requestedAmount,
        BigDecimal approvedAmount,
        ChargeStatus chargeStatus,
        String tossBankCode,
        LocalDateTime requestedAt,
        LocalDateTime approvedAt,
        LocalDateTime failedAt
) {

    /**
     * application 寃곌낵瑜?presentation ?묐떟?쇰줈 蹂?섑븳??
     */
    public static ChargeListItemResponse from(ChargeListItemResult result) {
        return new ChargeListItemResponse(
                result.chargeId(),
                result.requestedAmount(),
                result.approvedAmount(),
                result.chargeStatus(),
                result.tossBankCode(),
                result.requestedAt(),
                result.approvedAt(),
                result.failedAt()
        );
    }
}
