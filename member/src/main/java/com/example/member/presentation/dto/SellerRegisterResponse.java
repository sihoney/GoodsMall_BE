package com.example.member.presentation.dto;

import com.example.member.domain.entity.Seller;
import java.time.LocalDateTime;
import java.util.UUID;

public record SellerRegisterResponse(
        UUID sellerId,
        UUID memberId,
        LocalDateTime approvedAt
) {

    public static SellerRegisterResponse from(Seller seller) {
        return new SellerRegisterResponse(
                seller.getSellerId(),
                seller.getMemberId(),
                seller.getApprovedAt()
        );
    }
}
