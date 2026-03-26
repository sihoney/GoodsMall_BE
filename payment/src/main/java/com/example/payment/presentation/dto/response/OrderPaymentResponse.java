package com.example.payment.presentation.dto.response;

import com.example.payment.application.dto.OrderPaymentResult;
import com.example.payment.domain.enumtype.EscrowStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderPaymentResponse(
        UUID orderId,
        UUID buyerWalletId,
        UUID escrowId,
        Long paidAmount,
        Long buyerWalletBalance,
        EscrowStatus escrowStatus,
        LocalDateTime releaseAt
) {

    public static OrderPaymentResponse from(OrderPaymentResult result) {
        return new OrderPaymentResponse(
                result.orderId(),
                result.buyerWalletId(),
                result.escrowId(),
                result.paidAmount(),
                result.buyerWalletBalance(),
                result.escrowStatus(),
                result.releaseAt()
        );
    }
}
