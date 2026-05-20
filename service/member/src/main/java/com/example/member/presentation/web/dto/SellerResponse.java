package com.example.member.presentation.web.dto;

import com.example.member.application.dto.result.SellerResult;
import java.time.LocalDateTime;
import java.util.UUID;

public record SellerResponse(
        UUID sellerId,
        UUID memberId,
        String bankName,
        String account,
        LocalDateTime approvedAt
) {

    public static SellerResponse from(SellerResult result) {
        return new SellerResponse(
                result.sellerId(),
                result.memberId(),
                result.bankName(),
                result.account(),
                result.approvedAt()
        );
    }
}

