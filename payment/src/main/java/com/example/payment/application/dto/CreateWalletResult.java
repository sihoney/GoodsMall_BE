package com.example.payment.application.dto;

import java.util.UUID;

public record CreateWalletResult(
        UUID walletId,
        UUID memberId,
        Long balance,
        boolean created
) {
}
