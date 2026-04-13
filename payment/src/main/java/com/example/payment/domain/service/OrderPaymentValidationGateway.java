package com.example.payment.domain.service;

import java.util.UUID;

public interface OrderPaymentValidationGateway {

    OrderPaymentValidationData validate(UUID orderId, UUID buyerId, Long amount);
}
