package com.example.member.seller.presentation.web.dto;

import jakarta.validation.constraints.NotBlank;

public record SellerRegisterRequest(
        @NotBlank(message = "bankName은 필수입니다.")
        String bankName,
        @NotBlank(message = "account는 필수입니다.")
        String account
) {
}

