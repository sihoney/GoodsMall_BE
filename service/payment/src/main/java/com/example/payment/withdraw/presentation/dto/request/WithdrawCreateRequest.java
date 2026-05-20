package com.example.payment.withdraw.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record WithdrawCreateRequest(
        @NotNull(message = "湲덉븸? ?꾩닔?낅땲??")
        @DecimalMin(value = "0.01", message = "湲덉븸? 0蹂대떎 而ㅼ빞 ?⑸땲??")
        @Digits(integer = 19, fraction = 2)
        BigDecimal amount,

        @NotBlank(message = "怨꾩쥖踰덊샇???꾩닔?낅땲??")
        String bankAccount,

        @NotBlank(message = "?덇툑二쇰뒗 ?꾩닔?낅땲??")
        String accountHolder
) {
}
