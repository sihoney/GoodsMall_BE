package com.example.payment.payment.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * payment ?대? 二쇰Ц 寃곗젣 ?좎뒪耳?댁뒪 ?낅젰 command??
 * ?몃? order ?대깽?몃? seller蹂??뺤궛 ?⑥쐞濡??뺢퇋?뷀븳 寃곌낵瑜??대뒗??
 */
public record OrderPaymentCommand(
        UUID orderId,
        UUID buyerMemberId,
        BigDecimal orderAmount,
        List<OrderPaymentLineCommand> paymentLines
) {
}
