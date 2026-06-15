package com.example.payment.payment.domain.service;

import java.util.List;

public record OrderPaymentValidationData(
        List<OrderPaymentValidationItemData> orderItems
) {
}
