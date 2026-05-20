package com.example.member.application.dto.command;

public record SellerRegisterCommand(
        String bankName,
        String account
) {
}
