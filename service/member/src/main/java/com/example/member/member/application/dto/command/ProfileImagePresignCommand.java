package com.example.member.member.application.dto.command;

public record ProfileImagePresignCommand(
        String fileName,
        String contentType
) {
}
