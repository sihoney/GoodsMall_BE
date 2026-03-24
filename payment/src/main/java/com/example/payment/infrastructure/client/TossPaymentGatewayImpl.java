package com.example.payment.infrastructure.client;

import com.example.payment.domain.service.TossPaymentGateway;
import org.springframework.stereotype.Component;

@Component
public class TossPaymentGatewayImpl implements TossPaymentGateway {

    @Override
    public TossPaymentConfirmation confirm(String paymentKey, String orderId, Long amount) {
        throw new UnsupportedOperationException("Toss payment integration is not implemented yet.");
    }
}
