package com.example.member.seller.application.dto.command;

import jakarta.validation.constraints.NotBlank;

public record SellerRegisterCommand(
        @NotBlank
        String bankName,
        @NotBlank
        String account
) {
}
