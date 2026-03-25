package com.example.payment.presentation.dto.response;

import com.example.payment.application.dto.ChargeCreateResult;
import com.example.payment.domain.enumtype.ChargeStatus;
import com.example.payment.domain.enumtype.PgProvider;
import java.util.UUID;

public record ChargeCreateResponse(
        UUID chargeId,
        UUID walletId,
        String pgOrderId,
        Long amount,
        PgProvider pgProvider,
        ChargeStatus chargeStatus
) {

    public static ChargeCreateResponse from(ChargeCreateResult result) {
        return new ChargeCreateResponse(
                result.chargeId(),
                result.walletId(),
                result.pgOrderId(),
                result.amount(),
                result.pgProvider(),
                result.chargeStatus()
        );
    }
}
