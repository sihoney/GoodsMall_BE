package com.example.order.presentation.dto.request;

import com.example.order.domain.enumtype.RequesterType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderCancelRequest(
        @NotEmpty(message = "처리할 주문 상품을 1개 이상 선택해주세요.")
        @Valid
        List<ClaimItemRequest> items,

        @NotNull
        RequesterType requesterType
) {
}
