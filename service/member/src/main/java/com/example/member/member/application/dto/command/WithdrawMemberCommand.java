package com.example.member.member.application.dto.command;

import java.util.UUID;

public record WithdrawMemberCommand(
        UUID memberId,
        String currentPassword,
        String authorizationHeader
) {
}
