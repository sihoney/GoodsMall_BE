package com.example.payment.application.dto;

import com.example.payment.domain.enumtype.CardTransactionStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record CardPaymentConfirmResult(
        UUID transactionGroupId,
        UUID orderId,
        UUID buyerId,
        Long amount,
        CardTransactionStatus status,
        LocalDateTime approvedAt
) {
}
