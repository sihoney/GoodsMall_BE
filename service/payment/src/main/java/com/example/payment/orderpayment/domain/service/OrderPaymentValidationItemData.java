package com.example.payment.orderpayment.domain.service;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderPaymentValidationItemData(
        UUID orderItemId,
        UUID sellerId,
        BigDecimal lineAmount
) {
}
