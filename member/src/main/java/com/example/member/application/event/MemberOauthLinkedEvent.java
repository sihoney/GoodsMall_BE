package com.example.member.application.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record MemberOauthLinkedEvent(
        UUID memberId,
        String provider,
        String providerUserId,
        String providerEmail,
        String providerNickname,
        LocalDateTime linkedAt
) {
}
