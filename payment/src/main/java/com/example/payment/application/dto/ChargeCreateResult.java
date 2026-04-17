package com.example.payment.application.dto;

import com.example.payment.domain.enumtype.ChargeStatus;
import java.util.UUID;

public record ChargeCreateResult(
        UUID chargeId,
        UUID walletId,
        String pgOrderId,
        Long amount,
        ChargeStatus chargeStatus
) {
}
