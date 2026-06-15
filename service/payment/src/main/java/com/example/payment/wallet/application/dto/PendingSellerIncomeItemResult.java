package com.example.payment.wallet.application.dto;

import com.example.payment.escrow.domain.enumtype.EscrowStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ?먮ℓ???뺤궛 ?湲?escrow 紐⑸줉 議고쉶 寃곌낵??
 */
public record PendingSellerIncomeItemResult(
        UUID escrowId,
        UUID orderId,
        BigDecimal amount,
        EscrowStatus escrowStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
