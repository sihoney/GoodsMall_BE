package com.example.payment.presentation.dto.response;

import com.example.payment.application.dto.OrderPaymentResult;
import com.example.payment.domain.enumtype.EscrowStatus;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 주문 결제 API의 응답 DTO다.
 */
public record OrderPaymentResponse(
        UUID orderId,
        UUID buyerWalletId,
        UUID escrowId,
        Long paidAmount,
        Long buyerWalletBalance,
        EscrowStatus escrowStatus,
        LocalDateTime releaseAt
) {

    /**
     * application 결과를 presentation 응답 형식으로 변환한다.
     */
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
