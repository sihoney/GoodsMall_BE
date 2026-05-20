package com.example.payment.card.presentation.dto.response;

import com.example.payment.card.application.dto.CardPaymentConfirmResult;
import com.example.payment.card.domain.enumtype.CardTransactionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CardPaymentConfirmResponse(
        UUID transactionGroupId,
        UUID orderId,
        UUID buyerId,
        BigDecimal amount,
        CardTransactionStatus status,
        LocalDateTime approvedAt
) {

    public static CardPaymentConfirmResponse from(CardPaymentConfirmResult result) {
        return new CardPaymentConfirmResponse(
                result.transactionGroupId(),
                result.orderId(),
                result.buyerId(),
                result.amount(),
                result.status(),
                result.approvedAt()
        );
    }
}
