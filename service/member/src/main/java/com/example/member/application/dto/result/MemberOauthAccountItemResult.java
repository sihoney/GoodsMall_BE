package com.example.member.application.dto.result;

import java.time.LocalDateTime;
import java.util.UUID;

public record MemberOauthAccountItemResult(
        UUID oauthAccountId,
        String provider,
        String providerEmail,
        String providerNickname,
        LocalDateTime linkedAt,
        LocalDateTime updatedAt,
        boolean canUnlink
) {
}
