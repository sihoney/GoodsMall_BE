package com.example.member.application.dto.command;

public record LoginCommand(
        String email,
        String password
) {
}
