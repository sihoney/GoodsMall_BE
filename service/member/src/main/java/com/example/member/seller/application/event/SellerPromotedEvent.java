package com.example.member.seller.application.event;

import java.util.UUID;

public record SellerPromotedEvent(
        UUID memberId,
        UUID sellerId,
        String bankName
) {
}
