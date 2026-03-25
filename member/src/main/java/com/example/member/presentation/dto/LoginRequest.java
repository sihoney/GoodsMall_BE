package com.example.member.presentation.dto;

public record LoginRequest(
        String email,
        String password
) {
}
