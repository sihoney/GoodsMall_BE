package com.example.payment.application.dto;

import com.example.payment.domain.enumtype.ConfirmationType;
import java.util.UUID;

public record EscrowReleaseCommand(
        UUID orderId,
        UUID sellerMemberId,
        ConfirmationType confirmationType
) {
}
