package com.example.member.presentation.web.dto;

import jakarta.validation.constraints.NotBlank;

public record WithdrawMemberRequest(
        @NotBlank
        String currentPassword
) {
}
