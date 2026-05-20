package com.example.payment.payment.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * payment ?대? 二쇰Ц 寃곗젣 寃곌낵??
 * ?ㅼ쨷 seller 二쇰Ц??吏?먰븯湲??꾪빐 ?앹꽦??escrow ?앸퀎?먮? 紐⑸줉?쇰줈 諛섑솚?쒕떎.
 */
public record OrderPaymentResult(
        UUID orderId,
        UUID buyerWalletId,
        List<UUID> escrowIds,
        BigDecimal paidAmount,
        BigDecimal buyerWalletBalance
) {
}
