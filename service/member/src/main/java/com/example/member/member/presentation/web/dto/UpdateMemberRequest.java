package com.example.member.member.presentation.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateMemberRequest(
        @NotBlank
        @Size(max = 30)
        String nickname,
        String phone,
        String address,
        String profileImageKey
) {
}

