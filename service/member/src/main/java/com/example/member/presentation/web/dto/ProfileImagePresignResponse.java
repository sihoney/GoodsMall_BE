package com.example.member.presentation.web.dto;

import com.example.member.application.dto.result.ProfileImagePresignResult;

public record ProfileImagePresignResponse(
        String objectKey,
        String uploadUrl,
        long expiresIn
) {
    public static ProfileImagePresignResponse from(ProfileImagePresignResult result) {
        return new ProfileImagePresignResponse(
                result.objectKey(),
                result.uploadUrl(),
                result.expiresIn()
        );
    }
}

