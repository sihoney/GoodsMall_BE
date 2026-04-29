package com.example.member.infrastructure.storage.s3;

import com.example.member.application.port.out.ProfileImageUrlPort;
import com.example.member.config.S3Properties;
import java.time.Duration;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Component
public class ProfileImageUrlResolver implements ProfileImageUrlPort {

    private final String profileImagePrefix;
    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

    public ProfileImageUrlResolver(S3Presigner s3Presigner, S3Properties s3Properties) {
        this.s3Presigner = s3Presigner;
        this.s3Properties = s3Properties;
        this.profileImagePrefix = trimTrailingSlash(s3Properties.getProfileImagePrefix());
    }

    @Override
    public boolean isSupportedKey(String objectKey) {
        return objectKey != null
                && !objectKey.isBlank()
                && objectKey.startsWith(profileImagePrefix + "/");
    }

    @Override
    public String resolve(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return null;
        }
        if (!isSupportedKey(objectKey)) {
            throw new IllegalArgumentException("profileImageKey가 프로필 이미지 경로가 아닙니다.");
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(s3Properties.getGetPresignExpirationSeconds()))
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
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
