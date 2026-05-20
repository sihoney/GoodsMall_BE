package com.example.member.member.application.event;

import java.util.UUID;

public record MemberSignedUpEvent(
        UUID memberId,
        String email
) {
}
