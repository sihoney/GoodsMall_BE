package com.example.payment.orderpayment.domain.service;

import java.math.BigDecimal;
import java.util.UUID;

public interface OrderPaymentValidationGateway {

    OrderPaymentValidationData validate(UUID orderId, UUID buyerId, BigDecimal amount);
}
