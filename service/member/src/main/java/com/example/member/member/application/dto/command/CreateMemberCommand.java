package com.example.member.member.application.dto.command;

import com.todaylunch.common.security.auth.enumtype.MemberRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateMemberCommand(
        @NotBlank
        String email,
        @NotBlank
        @Size(min = 8)
        String password,
        @NotBlank
        String nickname,
        String phone,
        String address,
        String profileImageKey,
        MemberRole role
) {
}
