package com.example.member.member.application.dto.command;

import jakarta.validation.constraints.NotBlank;

public record ProfileImagePresignCommand(
        @NotBlank
        String fileName,
        @NotBlank
        String contentType
) {
}
