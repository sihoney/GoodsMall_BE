package com.example.settlement.presentation.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/**
 * 판매자 부분 정산 실행 요청이다.
 */
public record PartialSettlementExecutionRequest(
        @NotEmpty(message = "settlementItemIds must not be empty.")
        List<@NotNull(message = "settlementItemId must not be null.") UUID> settlementItemIds
) {
}
