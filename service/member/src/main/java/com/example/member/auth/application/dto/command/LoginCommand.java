package com.example.member.auth.application.dto.command;

import com.example.member.common.application.dto.AuthSessionMetadata;

public record LoginCommand(
        String email,
        String password,
        AuthSessionMetadata authSessionMetadata
) {
}
