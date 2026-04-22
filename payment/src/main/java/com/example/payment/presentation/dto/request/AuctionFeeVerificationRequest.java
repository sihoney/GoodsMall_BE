package com.example.payment.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record AuctionFeeVerificationRequest(
        @NotNull(message = "bidId는 필수입니다.")
        UUID bidId,

        @NotNull(message = "auctionId는 필수입니다.")
        UUID auctionId,

        UUID previousBidderId,

        @Digits(integer = 19, fraction = 2, message = "previousBidderPaidFee 형식이 올바르지 않습니다.")
        @DecimalMin(value = "0.01", message = "previousBidderPaidFee는 0보다 커야 합니다.")
        BigDecimal previousBidderPaidFee,

        @NotNull(message = "highestBidderId는 필수입니다.")
        UUID highestBidderId,

        @NotNull(message = "highestBidderFee는 필수입니다.")
        @Digits(integer = 19, fraction = 2, message = "highestBidderFee 형식이 올바르지 않습니다.")
        @DecimalMin(value = "0.01", message = "highestBidderFee는 0보다 커야 합니다.")
        BigDecimal highestBidderFee
) {
}
