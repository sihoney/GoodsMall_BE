package com.example.member.member.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UpdateMemberCommand(
        @NotNull
        UUID memberId,
        @NotBlank
        String nickname,
        String phone,
        String address,
        String profileImageKey
) {
}
