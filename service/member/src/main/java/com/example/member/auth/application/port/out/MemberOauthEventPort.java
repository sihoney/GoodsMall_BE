package com.example.member.auth.application.port.out;

import java.time.LocalDateTime;
import java.util.UUID;

public interface MemberOauthEventPort {

    void publishMemberOauthLinked(
            UUID memberId,
            String provider,
            String providerUserId,
            String providerEmail,
            String providerNickname,
            LocalDateTime linkedAt
    );
}
