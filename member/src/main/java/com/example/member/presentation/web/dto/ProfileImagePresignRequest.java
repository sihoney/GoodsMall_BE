package com.example.member.presentation.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ProfileImagePresignRequest(
        @NotBlank(message = "fileName은 필수입니다.")
        String fileName,
        @NotBlank(message = "contentType은 필수입니다.")
        String contentType
) {
}

