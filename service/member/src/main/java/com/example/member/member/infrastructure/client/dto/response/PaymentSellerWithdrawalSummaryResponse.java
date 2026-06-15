package com.example.member.member.infrastructure.client.dto.response;

public record PaymentSellerWithdrawalSummaryResponse(
        boolean hasPendingIncome,
        boolean hasPendingWithdrawRequest
) {
}
