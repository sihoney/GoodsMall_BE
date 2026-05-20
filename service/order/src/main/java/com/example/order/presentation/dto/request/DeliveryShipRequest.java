package com.example.order.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DeliveryShipRequest(
        @NotBlank String courier,
        @NotBlank String invoiceNumber
) {
}
