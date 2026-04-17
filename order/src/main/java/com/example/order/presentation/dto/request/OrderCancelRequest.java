package com.example.order.presentation.dto.request;

import com.example.order.domain.enumtype.RequesterType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderCancelRequest(
        @NotBlank
        String reason,

        String detailReason,

        @NotNull
        RequesterType requesterType
) {
}
