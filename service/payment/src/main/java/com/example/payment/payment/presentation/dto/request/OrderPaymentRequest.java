package com.example.payment.payment.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 二쇰Ц 寃곗젣 API???낅젰 DTO??
 * 援щℓ??寃곗젣 ?뺣낫? seller ?뺤궛 ?뺣낫瑜??④퍡 ?대뒗??
 */
public record OrderPaymentRequest(
        @NotNull(message = "二쇰Ц ID???꾩닔?낅땲??")
        UUID orderId,

        @NotNull(message = "援щℓ???뚯썝 ID???꾩닔?낅땲??")
        UUID buyerMemberId,

        @NotNull(message = "?먮ℓ???뚯썝 ID???꾩닔?낅땲??")
        UUID sellerMemberId,

        @NotNull(message = "二쇰Ц 湲덉븸? ?꾩닔?낅땲??")
        @DecimalMin(value = "0.01", message = "二쇰Ц 湲덉븸? 0蹂대떎 而ㅼ빞 ?⑸땲??")
        @Digits(integer = 19, fraction = 2)
        BigDecimal orderAmount,

        @NotNull(message = "?먮ℓ???뺤궛 媛??湲덉븸? ?꾩닔?낅땲??")
        @DecimalMin(value = "0.01", message = "?먮ℓ???뺤궛 媛??湲덉븸? 0蹂대떎 而ㅼ빞 ?⑸땲??")
        @Digits(integer = 19, fraction = 2)
        BigDecimal sellerReceivableAmount,

        LocalDateTime releaseAt
) {
}
