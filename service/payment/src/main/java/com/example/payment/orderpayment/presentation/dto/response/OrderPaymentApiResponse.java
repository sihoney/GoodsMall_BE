package com.example.payment.orderpayment.presentation.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * 二쇰Ц 寃곗젣 API ?묐떟 DTO??
 * 湲곗〈 Kafka OrderPaymentResult 怨꾩빟怨??좎궗???뺥깭濡?諛섑솚?쒕떎.
 */
public record OrderPaymentApiResponse(
        UUID orderId,
        UUID buyerMemberId,
        BigDecimal amount,
        String status,
        String reasonCode
) {
}
