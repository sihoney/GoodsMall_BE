package com.example.product.application.service;

import com.example.product.application.usecase.ProductImageUpdateUseCase;
import com.example.product.common.exception.ImageNotOwnedByProductException;
import com.example.product.common.exception.ProductImageNotFoundException;
import com.example.product.common.exception.ProductNotFoundException;
import com.example.product.domain.entity.Product;
import com.example.product.domain.entity.ProductImage;
import com.example.product.domain.repository.ProductImageRepository;
import com.example.product.domain.repository.ProductRepository;
import com.example.product.infrastructure.messaging.kafka.ProductOutboxEventService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductImageUpdateService implements ProductImageUpdateUseCase {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductOutboxEventService productOutboxEventService;

    @Override
    public void changeThumbnail(UUID productId, UUID imageId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);

        ProductImage newThumbnail = productImageRepository.findById(imageId)
                .orElseThrow(ProductImageNotFoundException::new);

        if (!newThumbnail.getProductId().equals(productId)) {
            throw new ImageNotOwnedByProductException();
        }

        productImageRepository.findThumbnailByProductId(productId)
                .ifPresent(current -> {
                    current.unmarkThumbnail();
                    productImageRepository.save(current);
                });

        newThumbnail.markAsThumbnail();
        productImageRepository.save(newThumbnail);

        productOutboxEventService.saveUpdatedEvent(product);

        log.info("Thumbnail changed: productId={}, imageId={}", productId, imageId);
    }
}
