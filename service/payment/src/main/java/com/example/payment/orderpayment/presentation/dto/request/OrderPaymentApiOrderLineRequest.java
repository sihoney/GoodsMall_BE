package com.example.payment.orderpayment.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * 二쇰Ц 寃곗젣 API ?붿껌??二쇰Ц ?쇱씤 DTO??
 * Kafka ?붿껌 怨꾩빟怨?理쒕????좎궗?섍쾶 ?좎???seller 吏묎퀎 ?낅젰?쇰줈 ?ъ슜?쒕떎.
 */
public record OrderPaymentApiOrderLineRequest(
        @NotNull(message = "二쇰Ц ??ぉ ID???꾩닔?낅땲??")
        UUID orderItemId,

        @NotNull(message = "?먮ℓ??ID???꾩닔?낅땲??")
        UUID sellerId,

        @NotNull(message = "二쇰Ц ?쒖젏 ?④????꾩닔?낅땲??")
        @Positive(message = "二쇰Ц ?쒖젏 ?④???0蹂대떎 而ㅼ빞 ?⑸땲??")
        BigDecimal unitPriceSnapshot,

        @NotNull(message = "?섎웾? ?꾩닔?낅땲??")
        @Positive(message = "?섎웾? 0蹂대떎 而ㅼ빞 ?⑸땲??")
        Integer quantity,

        @NotNull(message = "二쇰Ц ??ぉ 珥앹븸? ?꾩닔?낅땲??")
        @Positive(message = "二쇰Ц ??ぉ 珥앹븸? 0蹂대떎 而ㅼ빞 ?⑸땲??")
        BigDecimal lineTotalPrice
) {
}
