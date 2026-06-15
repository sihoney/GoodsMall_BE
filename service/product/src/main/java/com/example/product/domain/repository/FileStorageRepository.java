package com.example.product.domain.repository;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageRepository {

    String uploadImage(MultipartFile file);

    void deleteImage(String imageKey);

    String getImageUrl(String imageKey);

    String generatePresignedUrl(String imageKey);
}
