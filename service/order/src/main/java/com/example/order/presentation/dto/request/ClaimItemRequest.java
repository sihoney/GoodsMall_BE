package com.example.order.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ClaimItemRequest(
        @NotNull(message = "주문 상품 ID를 입력해주세요.")
        UUID orderItemId,

        @NotBlank(message = "사유를 입력해주세요.")
        String reason,

        String detailReason
) {
}
