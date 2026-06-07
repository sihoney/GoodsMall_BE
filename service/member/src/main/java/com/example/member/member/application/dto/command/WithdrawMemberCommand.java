package com.example.member.member.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record WithdrawMemberCommand(
        @NotNull
        UUID memberId,
        @NotBlank
        String currentPassword,
        @NotBlank
        String authorizationHeader
) {
}
