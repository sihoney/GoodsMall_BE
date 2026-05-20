package com.example.member.auth.application.dto.command;

public record LoginCommand(
        String email,
        String password
) {
}
