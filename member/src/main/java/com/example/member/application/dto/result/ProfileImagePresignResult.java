package com.example.member.application.dto.result;

public record ProfileImagePresignResult(
        String objectKey,
        String uploadUrl,
        long expiresIn
) {
}
