package com.example.member.verification.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AccountVerificationConfirmCommand(
        @NotBlank
        @Pattern(regexp = "\\d{6}")
        String code
) {
}
