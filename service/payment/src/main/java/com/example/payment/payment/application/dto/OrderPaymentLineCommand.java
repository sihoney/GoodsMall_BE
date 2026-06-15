package com.example.payment.payment.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 二쇰Ц 寃곗젣 ??escrow瑜??앹꽦??orderItem ?⑥쐞 ?낅젰?대떎.
 */
public record OrderPaymentLineCommand(
        UUID orderItemId,
        UUID sellerMemberId,
        BigDecimal lineAmount
) {
}
