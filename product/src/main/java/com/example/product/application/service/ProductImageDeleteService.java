package com.example.product.application.service;

import com.example.product.application.usecase.ProductImageDeleteUseCase;
import com.example.product.common.exception.ImageNotOwnedByProductException;
import com.example.product.common.exception.ProductImageNotFoundException;
import com.example.product.common.exception.ProductNotFoundException;
import com.example.product.domain.entity.Product;
import com.example.product.domain.entity.ProductImage;
import com.example.product.domain.repository.ProductImageRepository;
import com.example.product.domain.repository.ProductRepository;
import com.example.product.domain.repository.FileStorageRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductImageDeleteService implements ProductImageDeleteUseCase {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final FileStorageRepository fileStorageRepository;

    @Override
    public void deleteImage(UUID productId, UUID imageId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);

        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(ProductImageNotFoundException::new);

        validateSameProductId(productId, image);

        boolean wasThumbnail = image.isThumbnail();

        try {
            fileStorageRepository.deleteImage(image.getS3Key());
            log.info("S3 image deleted: s3Key={}", image.getS3Key());
        } catch (Exception e) {
            log.error("Failed to delete S3 image: s3Key={}", image.getS3Key(), e);
        }

        productImageRepository.deleteById(imageId);
        log.info("ProductImage deleted: imageId={}, productId={}", imageId, productId);

        if (wasThumbnail) {
            reassignThumbnail(productId);
        }
    }

    private static void validateSameProductId(UUID productId, ProductImage image) {
        if (!image.getProductId().equals(productId)) {
            throw new ImageNotOwnedByProductException();
        }
    }

    private void reassignThumbnail(UUID productId) {
        List<ProductImage> remainingImages = productImageRepository.findByProductId(productId);

        if (!remainingImages.isEmpty()) {
            ProductImage firstImage = remainingImages.getFirst();
            firstImage.markAsThumbnail();
            productImageRepository.save(firstImage);
            log.info("Thumbnail reassigned: imageId={}, productId={}", firstImage.getImageId(), productId);
        } else {
            log.info("No images left for product: productId={}", productId);
        }
    }
}
