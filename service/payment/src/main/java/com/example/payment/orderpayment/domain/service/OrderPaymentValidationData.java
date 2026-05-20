package com.example.payment.orderpayment.domain.service;

import java.util.List;

public record OrderPaymentValidationData(
        List<OrderPaymentValidationItemData> orderItems
) {
}
