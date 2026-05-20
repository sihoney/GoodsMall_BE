package com.example.payment.wallet.application.dto;

import com.example.payment.wallet.domain.enumtype.ChargeStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChargeConfirmResult(
        UUID chargeId,
        ChargeStatus chargeStatus,
        BigDecimal approvedAmount,
        BigDecimal walletBalance,
        LocalDateTime approvedAt
) {
}
