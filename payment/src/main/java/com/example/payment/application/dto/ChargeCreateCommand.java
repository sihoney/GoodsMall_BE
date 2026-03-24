package com.example.payment.application.dto;

import com.example.payment.domain.enumtype.PgProvider;
import java.util.UUID;

public record ChargeCreateCommand(
        UUID memberId,
        Long amount,
        PgProvider pgProvider
) {
}
