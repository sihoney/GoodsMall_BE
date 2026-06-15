package com.example.payment.wallet.presentation.dto.response;

public record PaymentSellerWithdrawalSummaryResponse(
        boolean hasPendingIncome,
        boolean hasPendingWithdrawRequest
) {
}
