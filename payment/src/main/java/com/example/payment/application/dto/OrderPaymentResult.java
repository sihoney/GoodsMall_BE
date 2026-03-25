package com.example.payment.application.dto;

import com.example.payment.domain.enumtype.EscrowStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderPaymentResult(
        UUID orderId,
        UUID buyerWalletId,
        UUID escrowId,
        Long paidAmount,
        Long buyerWalletBalance,
        EscrowStatus escrowStatus,
        LocalDateTime releaseAt
) {
}
