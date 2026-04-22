package com.example.payment.presentation.dto.response;

import com.example.payment.application.dto.OrderPaymentResult;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * 주문 결제 API 응답 DTO다.
 * 다중 seller 주문을 지원하므로 escrowId 단건 대신 escrowIds 목록을 반환한다.
 */
public record OrderPaymentResponse(
        UUID orderId,
        UUID buyerWalletId,
        List<UUID> escrowIds,
        BigDecimal paidAmount,
        BigDecimal buyerWalletBalance
) {

    public static OrderPaymentResponse from(OrderPaymentResult result) {
        return new OrderPaymentResponse(
                result.orderId(),
                result.buyerWalletId(),
                result.escrowIds(),
                result.paidAmount(),
                result.buyerWalletBalance()
        );
    }
}
