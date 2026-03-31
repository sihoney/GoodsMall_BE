package com.example.product.domain.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageUploadService {

    String uploadImage(MultipartFile file);

    void deleteImage(String imageKey);

    /**
     * 이미지 URL 생성
     *
     * @param imageKey 이미지 키
     * @return 이미지 접근 URL
     */
    String getImageUrl(String imageKey);

    /**
     * Presigned URL 생성 (Private 버킷용)
     *
     * @param imageKey 이미지 키
     * @return 임시 접근 가능한 Presigned URL
     */
    String generatePresignedUrl(String imageKey);
}
