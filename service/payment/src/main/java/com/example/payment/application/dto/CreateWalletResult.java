package com.example.payment.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateWalletResult(
        UUID walletId,
        UUID memberId,
        BigDecimal balance,
        boolean created
) {
}
