package com.example.payment.card.application.dto;

import com.example.payment.card.domain.enumtype.CardTransactionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CardPaymentConfirmResult(
        UUID transactionGroupId,
        UUID orderId,
        UUID buyerId,
        BigDecimal amount,
        CardTransactionStatus status,
        LocalDateTime approvedAt
) {
}
