package com.example.member.application.event;

import java.time.Instant;
import java.util.UUID;

public record SellerPromotedEvent(
        UUID eventId,
        UUID memberId,
        UUID sellerId,
        String bankName,
        Instant occurredAt
) {
}
