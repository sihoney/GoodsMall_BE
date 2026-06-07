package com.example.member.verification.application.dto.command;

import jakarta.validation.constraints.NotBlank;

public record AccountVerificationCreateCommand(
        @NotBlank
        String bankName,
        @NotBlank
        String accountNumber
) {
}
