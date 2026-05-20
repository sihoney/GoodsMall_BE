package com.example.payment.escrow.application.dto;

import com.example.payment.common.domain.enumtype.ConfirmationType;
import java.util.UUID;

public record EscrowReleaseCommand(
        UUID orderId,
        UUID sellerMemberId,
        ConfirmationType confirmationType
) {
}
