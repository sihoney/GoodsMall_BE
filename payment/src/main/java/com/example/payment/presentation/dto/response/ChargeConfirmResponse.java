package com.example.payment.presentation.dto.response;

import com.example.payment.application.dto.ChargeConfirmResult;
import com.example.payment.domain.enumtype.ChargeStatus;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 충전 승인 API의 응답 DTO다.
 */
public record ChargeConfirmResponse(
        UUID chargeId,
        ChargeStatus chargeStatus,
        Long approvedAmount,
        Long walletBalance,
        LocalDateTime approvedAt
) {

    /**
     * application 결과를 presentation 응답 형식으로 변환한다.
     */
    public static ChargeConfirmResponse from(ChargeConfirmResult result) {
        return new ChargeConfirmResponse(
                result.chargeId(),
                result.chargeStatus(),
                result.approvedAmount(),
                result.walletBalance(),
                result.approvedAt()
        );
    }
}
