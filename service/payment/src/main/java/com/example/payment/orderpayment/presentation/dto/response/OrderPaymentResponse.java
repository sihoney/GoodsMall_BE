package com.example.payment.orderpayment.presentation.dto.response;

import com.example.payment.orderpayment.application.dto.OrderPaymentResult;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * 二쇰Ц 寃곗젣 API ?묐떟 DTO??
 * ?ㅼ쨷 seller 二쇰Ц??吏?먰븯誘濡?escrowId ?④굔 ???escrowIds 紐⑸줉??諛섑솚?쒕떎.
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
