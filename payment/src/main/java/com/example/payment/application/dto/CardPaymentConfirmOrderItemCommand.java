package com.example.payment.application.dto;

import java.util.UUID;

public record CardPaymentConfirmOrderItemCommand(
        UUID orderItemId,
        Long amount
) {
}
