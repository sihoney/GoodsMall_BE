package com.example.payment.application.dto;

import java.util.UUID;

public record ChargeCreateCommand(
        UUID memberId,
        Long amount
) {
}
