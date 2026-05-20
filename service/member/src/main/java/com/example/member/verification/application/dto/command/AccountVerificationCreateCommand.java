package com.example.member.verification.application.dto.command;

public record AccountVerificationCreateCommand(
        String bankName,
        String accountNumber
) {
}
