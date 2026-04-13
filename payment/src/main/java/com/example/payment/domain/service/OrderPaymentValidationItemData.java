package com.example.payment.domain.service;

import java.util.UUID;

public record OrderPaymentValidationItemData(
        UUID orderItemId,
        UUID sellerId,
        Long lineAmount
) {
}
