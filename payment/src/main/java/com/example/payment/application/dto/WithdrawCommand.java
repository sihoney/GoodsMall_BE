package com.example.payment.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record WithdrawCommand(
        UUID memberId,
        BigDecimal amount,
        String bankAccount,
        String accountHolder
) {
}
