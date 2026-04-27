package com.example.member.presentation.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.todaylunch.common.security.auth.enumtype.MemberRole;

public record CreateMemberRequest(
        @NotBlank
        @Email
        String email,
        @NotBlank
        @Size(min = 8, max = 100)
        String password,
        @NotBlank
        @Size(max = 30)
        String nickname,
        String phone,
        String address,
        String profileImageKey,
        MemberRole role
) {
}

