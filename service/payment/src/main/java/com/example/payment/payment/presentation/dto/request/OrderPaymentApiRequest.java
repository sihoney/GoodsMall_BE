package com.example.payment.payment.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 二쇰Ц 寃곗젣 API ?붿껌 DTO??
 * 湲곗〈 Kafka OrderPaymentRequested 怨꾩빟??HTTP body ?뺥깭濡???릿??
 */
public record OrderPaymentApiRequest(

        @NotNull(message = "二쇰Ц ID???꾩닔?낅땲??")
        UUID orderId,

        @NotNull(message = "援щℓ??ID???꾩닔?낅땲??")
        UUID buyerId,

        @NotNull(message = "珥?寃곗젣 湲덉븸? ?꾩닔?낅땲??")
        @Positive(message = "珥?寃곗젣 湲덉븸? 0蹂대떎 而ㅼ빞 ?⑸땲??")
        BigDecimal totalPrice,

        @NotNull(message = "?붿껌 ?쒓컖? ?꾩닔?낅땲??")
        Instant requestedAt,

        @NotEmpty(message = "二쇰Ц ?쇱씤? 鍮꾩뼱 ?덉쓣 ???놁뒿?덈떎.")
        List<@Valid OrderPaymentApiOrderLineRequest> orderLines
) {
}
