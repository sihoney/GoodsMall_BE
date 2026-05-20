package com.example.payment.wallet.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 異⑹쟾 ?붿껌 ?앹꽦 API???낅젰 DTO??
 */
public record ChargeCreateRequest(
        @NotNull(message = "湲덉븸? ?꾩닔?낅땲??")
        @DecimalMin(value = "1", message = "湲덉븸? 理쒖냼 1???댁긽?댁뼱???⑸땲??")
        @Digits(integer = 19, fraction = 0, message = "湲덉븸? ???⑥쐞 ?뺤닔?ъ빞 ?⑸땲??")
        BigDecimal amount
) {
}
