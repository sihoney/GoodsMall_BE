package com.example.payment.wallet.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChargeConfirmFailureRequest(
        @NotBlank(message = "二쇰Ц ID???꾩닔?낅땲??")
        String orderId,
        String code,
        @NotBlank(message = "?ㅽ뙣 硫붿떆吏???꾩닔?낅땲??")
        String message
) {
}
