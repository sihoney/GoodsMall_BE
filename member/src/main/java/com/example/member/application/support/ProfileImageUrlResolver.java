package com.example.member.application.support;

import com.example.member.config.S3Properties;
import org.springframework.stereotype.Component;

@Component
public class ProfileImageUrlResolver { // S3에 저장된 회원 프로필 이미지의 URL을 생성하는 유틸리티 클래스

    private final String profileImagePrefix;
    private final String publicBaseUrl;
    private final String bucket;
    private final String region;

    public ProfileImageUrlResolver(S3Properties s3Properties) {
        this.profileImagePrefix = trimTrailingSlash(s3Properties.getProfileImagePrefix());
        this.publicBaseUrl = trimTrailingSlash(s3Properties.getPublicBaseUrl());
        this.bucket = s3Properties.getBucket();
        this.region = s3Properties.getRegion();
    }

    // S3 객체 키가 이 클래스에서 지원하는 형식인지 확인하는 메서드
    public boolean isSupportedKey(String objectKey) {
        return objectKey != null
                && !objectKey.isBlank()
                && objectKey.startsWith(profileImagePrefix + "/");
    }

    // S3 객체 키를 입력받아 해당 객체의 URL을 생성하는 메서드
    public String resolve(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return null;
        }
        if (!isSupportedKey(objectKey)) {
            throw new IllegalArgumentException("profileImageKey is invalid.");
        }
        if (publicBaseUrl != null && !publicBaseUrl.isBlank()) {
            return publicBaseUrl + "/" + objectKey;
        }
        return "https://%s.s3.%s.amazonaws.com/%s".formatted(bucket, region, objectKey);
    }

    private String trimTrailingSlash(String value) {
        if (value == null) {
            return null;
        }
        return value.endsWith("/") 
            ? value.substring(0, value.length() - 1) 
            : value;
    }
}
