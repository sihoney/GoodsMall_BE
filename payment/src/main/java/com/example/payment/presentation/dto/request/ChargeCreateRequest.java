package com.example.payment.presentation.dto.request;

import com.example.payment.domain.enumtype.PgProvider;

public record ChargeCreateRequest(
        Long amount,
        PgProvider pgProvider
) {
}
