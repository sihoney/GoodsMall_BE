package com.example.product.application.service;

import com.example.product.application.usecase.ProductImageUploadUseCase;
import com.example.product.common.exception.ProductNotFoundException;
import com.example.product.domain.entity.Product;
import com.example.product.domain.entity.ProductImage;
import com.example.product.domain.repository.ProductImageRepository;
import com.example.product.domain.repository.ProductRepository;
import com.example.product.domain.service.ImageUploadService;
import com.example.product.presentation.dto.response.ProductImageResponse;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductImageUploadService implements ProductImageUploadUseCase {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ImageUploadService imageUploadService;

    @Override
    @Transactional
    public ProductImageResponse uploadImage(
            UUID productId,
            MultipartFile file,
            Integer sortOrder,
            Boolean isThumbnail
    ) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
        if (product == null) {
            throw new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId);
        }

        if (product.isDeleted()) {
            throw new IllegalArgumentException("삭제된 상품에는 이미지를 추가할 수 없습니다");
        }

        if (Boolean.TRUE.equals(isThumbnail)) {
            Optional<ProductImage> existingThumbnail = productImageRepository.findThumbnailByProductId(productId);
            existingThumbnail.ifPresent(ProductImage::unmarkThumbnail);
        }

        String s3Key = imageUploadService.uploadImage(file);
        log.info("Image uploaded to S3: productId={}, s3Key={}", productId, s3Key);

        ProductImage productImage = ProductImage.create(
                UUID.randomUUID(),
                productId,
                s3Key,
                sortOrder,
                Boolean.TRUE.equals(isThumbnail),
                LocalDateTime.now()
        );

        ProductImage savedImage = productImageRepository.save(productImage);
        log.info("ProductImage saved: imageId={}", savedImage.getImageId());

        return ProductImageResponse.from(savedImage);
    }
}
