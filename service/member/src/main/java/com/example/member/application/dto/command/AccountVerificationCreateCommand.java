package com.example.member.application.dto.command;

public record AccountVerificationCreateCommand(
        String bankName,
        String accountNumber
) {
}
