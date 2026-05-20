package com.example.payment.refund.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRefundItemRequest(
        @NotNull(message = "二쇰Ц ??ぉ ID???꾩닔?낅땲??")
        UUID orderItemId,

        @NotNull(message = "?섎텋 湲덉븸? ?꾩닔?낅땲??")
        @DecimalMin(value = "1", message = "?섎텋 湲덉븸? 理쒖냼 1???댁긽?댁뼱???⑸땲??")
        @Digits(integer = 19, fraction = 0, message = "?섎텋 湲덉븸? ???⑥쐞 ?뺤닔?ъ빞 ?⑸땲??")
        BigDecimal refundAmount
) {
}
