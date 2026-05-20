package com.example.payment.charge.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * 異⑹쟾 ?뺤씤 API???낅젰 DTO??
 * PG ?뺤씤 寃곌낵? charge ?붿껌 ?뺣낫瑜??④퍡 ?대뒗??
 */
public record ChargeConfirmRequest(
        @NotNull(message = "異⑹쟾 ID???꾩닔?낅땲??")
        UUID chargeId,
        @NotBlank(message = "paymentKey???꾩닔?낅땲??")
        String paymentKey,
        @NotBlank(message = "二쇰Ц ID???꾩닔?낅땲??")
        String orderId,
        @NotNull(message = "湲덉븸? ?꾩닔?낅땲??")
        @DecimalMin(value = "1", message = "湲덉븸? 理쒖냼 1???댁긽?댁뼱???⑸땲??")
        @Digits(integer = 19, fraction = 0, message = "湲덉븸? ???⑥쐞 ?뺤닔?ъ빞 ?⑸땲??")
        BigDecimal amount
) {
}
