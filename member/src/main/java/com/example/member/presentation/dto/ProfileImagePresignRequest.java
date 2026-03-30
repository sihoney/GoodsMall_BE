package com.example.member.presentation.dto;

public record ProfileImagePresignRequest(
        String fileName,
        String contentType
) {
}
