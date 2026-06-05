package com.example.member.member.application.service;

import com.example.member.member.application.dto.command.ProfileImagePresignCommand;
import com.example.member.member.application.dto.result.ProfileImagePresignResult;
import com.example.member.member.infrastructure.storage.s3.S3Properties;
import java.net.URL;
import java.time.Duration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
public class ProfileImageService {

    private static final Map<String, String> EXTENSION_TO_CONTENT_TYPE = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "webp", "image/webp"
    );

    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

    public ProfileImageService(S3Presigner s3Presigner, S3Properties s3Properties) {
        this.s3Presigner = s3Presigner;
        this.s3Properties = s3Properties;
    }

    public ProfileImagePresignResult createPresignedUpload(ProfileImagePresignCommand command) {
        // [1] 요청 검증
        validateCommand(command);

        // [2] 입력 정규화
        String fileName = command.fileName().trim();
        String contentType = command.contentType().trim().toLowerCase(Locale.ROOT);

        // [3] 확장자 추출
        String extension = extractExtension(fileName);

        // [4] 타입 검증
        validateContentType(extension, contentType);

        // [5] 키 생성
        String objectKey = buildObjectKey(extension);

        // [6] S3 요청 생성
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Properties.getBucket()) // todaylunchmenu
                .key(objectKey)
                .contentType(contentType)
                .build();

        // [7] Presign 요청 생성
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(s3Properties.getPutPresignExpirationSeconds())) // 300
                .putObjectRequest(putObjectRequest)
                .build();

        // [8] URL 발급
        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        URL uploadUrl = presignedRequest.url();

        // [9] 결과 반환
        return new ProfileImagePresignResult(
                objectKey,
                uploadUrl.toString(),
                s3Properties.getPutPresignExpirationSeconds()
        );
    }

    private void validateCommand(ProfileImagePresignCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("프로필 이미지 업로드 요청은 필수입니다.");
        }
        if (command.fileName() == null || command.fileName().isBlank()) {
            throw new IllegalArgumentException("fileName은 필수입니다.");
        }
        if (command.contentType() == null || command.contentType().isBlank()) {
            throw new IllegalArgumentException("contentType은 필수입니다.");
        }
    }

    private String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            throw new IllegalArgumentException("fileName에는 지원하는 확장자가 포함되어야 합니다.");
        }
        String extension = fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        if (!EXTENSION_TO_CONTENT_TYPE.containsKey(extension)) {
            throw new IllegalArgumentException("지원하지 않는 파일 확장자입니다.");
        }
        return extension;
    }

    private void validateContentType(String extension, String contentType) {
        Set<String> allowedContentTypes = new HashSet<>(EXTENSION_TO_CONTENT_TYPE.values());
        if (!allowedContentTypes.contains(contentType)) {
            throw new IllegalArgumentException("지원하지 않는 contentType입니다.");
        }
        String expectedContentType = EXTENSION_TO_CONTENT_TYPE.get(extension);
        if (!expectedContentType.equals(contentType)) {
            throw new IllegalArgumentException("fileName 확장자와 contentType이 일치해야 합니다.");
        }
    }

    private String buildObjectKey(String extension) {
        String prefix = trimSlashes(s3Properties.getProfileImagePrefix());
        return prefix + "/" + UUID.randomUUID() + "." + extension;
    }

    private String trimSlashes(String value) {
        String trimmed = value == null ? "" : value.trim();
        while (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1);
        }
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("aws.s3.profile-image-prefix는 필수입니다.");
        }
        return trimmed;
    }
}
