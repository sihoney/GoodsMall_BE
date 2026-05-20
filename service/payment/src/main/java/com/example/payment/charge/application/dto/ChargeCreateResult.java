package com.example.payment.charge.application.dto;

import com.example.payment.charge.domain.enumtype.ChargeStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record ChargeCreateResult(
        UUID chargeId,
        UUID walletId,
        String pgOrderId,
        BigDecimal amount,
        ChargeStatus chargeStatus
) {
}
