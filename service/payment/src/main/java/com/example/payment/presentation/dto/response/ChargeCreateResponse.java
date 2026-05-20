package com.example.payment.presentation.dto.response;

import com.example.payment.application.dto.ChargeCreateResult;
import com.example.payment.domain.enumtype.ChargeStatus;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * 충전 요청 생성 API의 응답 DTO다.
 */
public record ChargeCreateResponse(
        UUID chargeId,
        UUID walletId,
        String pgOrderId,
        BigDecimal amount,
        ChargeStatus chargeStatus
) {

    /**
     * application 결과를 presentation 응답 형식으로 변환한다.
     */
    public static ChargeCreateResponse from(ChargeCreateResult result) {
        return new ChargeCreateResponse(
                result.chargeId(),
                result.walletId(),
                result.pgOrderId(),
                result.amount(),
                result.chargeStatus()
        );
    }
}
