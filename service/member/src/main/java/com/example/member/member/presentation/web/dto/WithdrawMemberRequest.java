package com.example.member.member.presentation.web.dto;

import jakarta.validation.constraints.NotBlank;

public record WithdrawMemberRequest(
        @NotBlank
        String currentPassword
) {
}
