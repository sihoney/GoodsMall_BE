package com.example.payment.application.dto;

import java.util.UUID;

public record EscrowReleaseCommand(
        UUID orderId,
        UUID sellerMemberId
) {
}
