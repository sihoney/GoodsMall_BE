package com.example.member.application.dto.command;

import java.util.UUID;

public record ChangePasswordCommand(
        UUID memberId,
        String currentPassword,
        String newPassword
) {
}
