package com.example.payment.presentation.dto.response;

import com.example.payment.application.dto.AuctionDepositResult;
import java.math.BigDecimal;
import java.util.UUID;

public record AuctionFeeVerificationResponse(
        UUID auctionId
) {

    public static AuctionFeeVerificationResponse success(AuctionDepositResult result) {
        return new AuctionFeeVerificationResponse(
                result.auctionId()
        );
    }
}
