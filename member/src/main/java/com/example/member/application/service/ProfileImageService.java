package com.example.member.application.service;

import com.example.member.config.S3Properties;
import com.example.member.presentation.dto.ProfileImagePresignRequest;
import com.example.member.presentation.dto.ProfileImagePresignResponse;
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

    // 지원하는 파일 확장자와 MIME 타입 매핑
    private static final Map<String, String> EXTENSION_TO_CONTENT_TYPE = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "webp", "image/webp"
    );

    private final S3Presigner s3Presigner; // AWS S3와의 상호작용을 위한 S3Presigner 클라이언트
    private final S3Properties s3Properties; // S3 관련 설정을 담은 S3Properties 클래스 (버킷 이름, 리전, 사전 서명 URL 만료 시간 등)

    public ProfileImageService(S3Presigner s3Presigner, S3Properties s3Properties) {
        this.s3Presigner = s3Presigner;
        this.s3Properties = s3Properties;
    }

    // 회원 프로필 이미지 업로드를 위한 사전 요청 URL을 생성하는 메서드
    // : 요청 DTO를 검증하고, S3 객체 키를 생성한 후, S3Presigner를 사용하여 사전 서명된 PUT URL을 생성하여 반환
    public ProfileImagePresignResponse createPresignedUpload(ProfileImagePresignRequest request) {
        validateRequest(request);

        String fileName = request.fileName().trim();
        String contentType = request.contentType().trim().toLowerCase(Locale.ROOT);
        String extension = extractExtension(fileName);
        validateContentType(extension, contentType);

        String objectKey = buildObjectKey(extension);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(s3Properties.getPutPresignExpirationSeconds()))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        URL uploadUrl = presignedRequest.url();

        return new ProfileImagePresignResponse(
                objectKey,
                uploadUrl.toString(),
                s3Properties.getPutPresignExpirationSeconds()
        );
    }

    // 요청 유효성 검사: 요청 객체, 파일 이름, 콘텐츠 타입이 모두 유효한지 확인
    private void validateRequest(ProfileImagePresignRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Profile image presign request body is required.");
        }
        if (request.fileName() == null || request.fileName().isBlank()) {
            throw new IllegalArgumentException("fileName is required.");
        }
        if (request.contentType() == null || request.contentType().isBlank()) {
            throw new IllegalArgumentException("contentType is required.");
        }
    }

    // 파일 이름에서 확장자를 추출하고 지원되는 확장자인지 확인
    private String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            throw new IllegalArgumentException("fileName must include a supported extension.");
        }
        String extension = fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        if (!EXTENSION_TO_CONTENT_TYPE.containsKey(extension)) {
            throw new IllegalArgumentException("Unsupported file extension.");
        }
        return extension;
    }

    // 콘텐츠 타입이 지원되는지 확인
    private void validateContentType(String extension, String contentType) {
        Set<String> allowedContentTypes = new HashSet<>(EXTENSION_TO_CONTENT_TYPE.values());
        if (!allowedContentTypes.contains(contentType)) {
            throw new IllegalArgumentException("Unsupported contentType.");
        }
        String expectedContentType = EXTENSION_TO_CONTENT_TYPE.get(extension);
        if (!expectedContentType.equals(contentType)) {
            throw new IllegalArgumentException("fileName extension and contentType do not match.");
        }
    }

    // S3 객체 키를 생성하는 유틸리티 메서드: 지정된 확장자를 사용하여 고유한 객체 키를 생성
    private String buildObjectKey(String extension) {
        String prefix = trimSlashes(s3Properties.getProfileImagePrefix());
        return prefix + "/" + UUID.randomUUID() + "." + extension;
    }

    // 문자열에서 앞뒤 슬래시를 제거하는 유틸리티 메서드: S3 객체 키의 접두사로 사용되는 문자열에서 불필요한 슬래시를 제거하여 일관된 형식 유지
    private String trimSlashes(String value) {
        String trimmed = value == null ? "" : value.trim();
        while (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1);
        }
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("aws.s3.profile-image-prefix is required.");
        }
        return trimmed;
    }
}
