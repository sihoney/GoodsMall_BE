package com.example.payment.presentation.dto.response;

import com.example.payment.application.dto.ChargeDetailResult;
import com.example.payment.domain.enumtype.ChargeStatus;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * charge 상세 조회 응답 DTO다.
 */
public record ChargeDetailResponse(
        UUID chargeId,
        UUID memberId,
        UUID walletId,
        Long requestedAmount,
        Long approvedAmount,
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
     * application 상세 결과를 presentation 응답으로 변환한다.
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
