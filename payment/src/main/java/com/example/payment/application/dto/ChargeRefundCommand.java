package com.example.payment.application.dto;

import java.util.UUID;

public record ChargeRefundCommand(
        UUID chargeId,
        String refundReason
) {
}
