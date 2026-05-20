package com.example.member.seller.application.dto.result;

import java.time.LocalDateTime;
import java.util.UUID;

public record SellerResult(
        UUID sellerId,
        UUID memberId,
        String bankName,
        String account,
        LocalDateTime approvedAt
) {
}
