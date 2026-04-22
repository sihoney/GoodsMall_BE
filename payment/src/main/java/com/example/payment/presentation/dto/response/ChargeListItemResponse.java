package com.example.payment.presentation.dto.response;

import com.example.payment.application.dto.ChargeListItemResult;
import com.example.payment.domain.enumtype.ChargeStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * charge 목록 단건 응답 DTO다.
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
     * application 결과를 presentation 응답으로 변환한다.
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
