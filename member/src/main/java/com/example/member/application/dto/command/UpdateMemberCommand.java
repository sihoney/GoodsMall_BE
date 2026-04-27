package com.example.member.application.dto.command;

import java.util.UUID;

public record UpdateMemberCommand(
        UUID memberId,
        String nickname,
        String phone,
        String address,
        String profileImageKey
) {
}
