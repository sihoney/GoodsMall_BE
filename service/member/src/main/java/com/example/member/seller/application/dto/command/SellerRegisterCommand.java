package com.example.member.seller.application.dto.command;

public record SellerRegisterCommand(
        String bankName,
        String account
) {
}
