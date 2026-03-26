package com.example.payment.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderPaymentCommand(
        UUID orderId,
        UUID buyerMemberId,
        UUID sellerMemberId,
        Long orderAmount,
        Long sellerReceivableAmount,
        LocalDateTime releaseAt
) {
}
