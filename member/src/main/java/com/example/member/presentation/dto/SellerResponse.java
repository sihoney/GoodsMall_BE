package com.example.member.presentation.dto;

import com.example.member.domain.entity.Seller;
import java.time.LocalDateTime;
import java.util.UUID;

public record SellerResponse(
        UUID sellerId,
        UUID memberId,
        String bankName,
        String account,
        LocalDateTime approvedAt
) {

    public static SellerResponse from(Seller seller) {
        return new SellerResponse(
                seller.getSellerId(),
                seller.getMemberId(),
                seller.getBankName(),
                seller.getAccount(),
                seller.getApprovedAt()
        );
    }
}
