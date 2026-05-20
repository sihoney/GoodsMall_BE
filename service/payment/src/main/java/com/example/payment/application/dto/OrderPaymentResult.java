package com.example.payment.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * payment 내부 주문 결제 결과다.
 * 다중 seller 주문을 지원하기 위해 생성된 escrow 식별자를 목록으로 반환한다.
 */
public record OrderPaymentResult(
        UUID orderId,
        UUID buyerWalletId,
        List<UUID> escrowIds,
        BigDecimal paidAmount,
        BigDecimal buyerWalletBalance
) {
}
