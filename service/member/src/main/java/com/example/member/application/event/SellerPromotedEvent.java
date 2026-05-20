package com.example.member.application.event;

import java.util.UUID;

public record SellerPromotedEvent(
        UUID memberId,
        UUID sellerId,
        String bankName
) {
}
