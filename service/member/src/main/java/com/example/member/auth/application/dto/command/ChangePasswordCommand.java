package com.example.member.auth.application.dto.command;

import java.util.UUID;

public record ChangePasswordCommand(
        UUID memberId,
        String currentPassword,
        String newPassword
) {
}
