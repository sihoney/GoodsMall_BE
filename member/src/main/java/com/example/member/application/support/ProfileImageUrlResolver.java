package com.example.member.application.support;

import com.example.member.config.S3Properties;
import java.time.Duration;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Component
public class ProfileImageUrlResolver { // S3에 저장된 회원 프로필 이미지의 URL을 생성하는 유틸리티 클래스

    private final String profileImagePrefix;
    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

    public ProfileImageUrlResolver(S3Presigner s3Presigner, S3Properties s3Properties) {
        this.s3Presigner = s3Presigner;
        this.s3Properties = s3Properties;
        this.profileImagePrefix = trimTrailingSlash(s3Properties.getProfileImagePrefix());
    }

    // S3 객체 키가 이 클래스에서 지원하는 형식인지 확인하는 메서드
    public boolean isSupportedKey(String objectKey) {
        return objectKey != null
                && !objectKey.isBlank()
                && objectKey.startsWith(profileImagePrefix + "/");
    }

    // S3 객체 키를 입력받아 해당 객체의 URL을 생성하는 메서드
    public String resolve(String objectKey) {
        // 1. 'objectKey'가 유효한지 확인
        if (objectKey == null || objectKey.isBlank()) {
            return null;
        }
        if (!isSupportedKey(objectKey)) {
            throw new IllegalArgumentException("profileImageKey is invalid.");
        }

        // 2. "이 key 파일을 읽는 S3 GET 요청" 생성
        // "이 파일을 읽는 요청을 만들겠다"는 메타정보 생성
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(objectKey)
                .build();

        // 3. 그 요청을 일정 시간만 유효한 presigned GET URL로 서명
        // "이 요청을 몇 초 동안 유효한 서명 URL로 만들지"를 정하는 객체
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(s3Properties.getGetPresignExpirationSeconds()))
                .getObjectRequest(getObjectRequest)
                .build();

        // 3. 최종 URL 문자열 반환
        // S3Presigner는 AWS SDK가 제공하는 "사전 서명 URL 생성기"
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
