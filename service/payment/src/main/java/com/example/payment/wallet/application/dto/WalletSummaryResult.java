package com.example.payment.wallet.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * wallet ?④굔 ?붿빟 議고쉶 寃곌낵??
 */
public record WalletSummaryResult(
        UUID walletId,
        UUID memberId,
        BigDecimal balance,
        LocalDateTime updatedAt
) {
}
