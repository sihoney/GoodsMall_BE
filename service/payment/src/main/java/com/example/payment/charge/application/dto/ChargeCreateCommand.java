package com.example.payment.charge.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ChargeCreateCommand(
        UUID memberId,
        BigDecimal amount
) {
}
