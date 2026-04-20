package com.example.payment.presentation.dto.response;

import com.example.payment.application.dto.AuctionDepositResult;
import java.math.BigDecimal;
import java.util.UUID;

public record AuctionFeeVerificationResponse(
        boolean success,
        UUID auctionId,
        String message
) {

    public static AuctionFeeVerificationResponse success(AuctionDepositResult result) {
        return new AuctionFeeVerificationResponse(
                true,
                result.auctionId(),
                "경매 예치금 차감 및 환불 처리가 완료되었습니다."
        );
    }
}
