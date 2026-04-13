package com.example.payment.application.dto;

import java.util.List;
import java.util.UUID;

public record CardPaymentConfirmCommand(
        UUID buyerId,
        UUID orderId,
        String paymentKey,
        Long amount,
        List<CardPaymentConfirmOrderItemCommand> orderItems
) {
}
