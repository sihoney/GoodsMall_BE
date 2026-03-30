package com.example.product.infrastructure.service;

import com.example.product.domain.service.ImageUploadService;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

/**
 * S3 이미지 업로드 Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3ImageUploadService implements ImageUploadService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.region.static}")
    private String region;

    /**
     * 이미지를 S3에 업로드하고 저장된 키를 반환
     *
     * @param file 업로드할 파일
     * @return S3 저장 경로 (키)
     */
    public String uploadImage(MultipartFile file) {
        validateFile(file);

        String s3Key = generateS3Key(extractExtension(file.getOriginalFilename()));

        try {
            // S3에 실제 업로드
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            log.info("S3 upload success: bucket={}, key={}, size={}", bucketName, s3Key, file.getSize());
            return s3Key;

        } catch (IOException e) {
            log.error("Failed to read file for S3 upload: {}", s3Key, e);
            throw new RuntimeException("이미지 파일을 읽는 중 오류가 발생했습니다", e);
        } catch (S3Exception e) {
            log.error("S3 error during upload: bucket={}, key={}", bucketName, s3Key, e);
            throw new RuntimeException("S3 업로드 중 오류가 발생했습니다: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    /**
     * S3에서 이미지 삭제
     *
     * @param s3Key 삭제할 S3 키
     */
    public void deleteImage(String s3Key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("S3 delete success: bucket={}, key={}", bucketName, s3Key);

        } catch (S3Exception e) {
            log.error("Failed to delete image from S3: bucket={}, key={}", bucketName, s3Key, e);
        }
    }

    /**
     * S3 URL 생성 (조회용)
     *
     * @param s3Key S3 키
     * @return 전체 URL
     */
    public String getImageUrl(String s3Key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);
    }

    /**
     * Presigned URL 생성 (Private 버킷용)
     * 15분 동안 유효한 임시 URL 생성
     *
     * @param s3Key S3 키
     * @return Presigned URL
     */
    public String generatePresignedUrl(String s3Key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(15))  // 15분 유효
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            String url = presignedRequest.url().toString();

            log.debug("Generated presigned URL: key={}, url={}", s3Key, url);
            return url;

        } catch (S3Exception e) {
            log.error("Failed to generate presigned URL: bucket={}, key={}", bucketName, s3Key, e);
            throw new RuntimeException("Presigned URL 생성 실패: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    /**
     * 파일 검증
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }

        // 파일 크기 검증 (5MB 제한)
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("파일 크기는 5MB를 초과할 수 없습니다");
        }

        // 파일 형식 검증
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다");
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg"; // 기본값
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * S3 키 생성 (경로 포함)
     */
    private String generateS3Key(String extension) {
        // 예: products/2026/03/30/uuid.jpg
        String date = java.time.LocalDate.now().toString().replace("-", "/");
        String uniqueId = UUID.randomUUID().toString();
        return String.format("products/%s/%s.%s", date, uniqueId, extension);
    }
}

