package com.example.payment.presentation.dto.response;

import com.example.payment.application.dto.CardPaymentConfirmResult;
import com.example.payment.domain.enumtype.CardTransactionStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record CardPaymentConfirmResponse(
        UUID transactionGroupId,
        UUID orderId,
        UUID buyerId,
        Long amount,
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
