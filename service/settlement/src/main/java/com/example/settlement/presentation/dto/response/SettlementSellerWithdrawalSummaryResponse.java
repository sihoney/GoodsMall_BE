package com.example.settlement.presentation.dto.response;

public record SettlementSellerWithdrawalSummaryResponse(
        boolean hasPendingSettlement,
        boolean hasProcessingSettlement,
        boolean hasPartialSettlementAvailable
) {
}
