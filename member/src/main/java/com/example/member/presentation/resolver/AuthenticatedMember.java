package com.example.member.presentation.resolver;

import com.example.member.domain.enumtype.MemberRole;
import java.util.UUID;

public record AuthenticatedMember(
        UUID memberId,
        MemberRole role
) {
}
