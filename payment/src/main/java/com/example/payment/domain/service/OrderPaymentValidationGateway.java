package com.example.payment.domain.service;

import java.util.UUID;

public interface OrderPaymentValidationGateway {

    boolean validate(UUID orderId, Long amount);
}
