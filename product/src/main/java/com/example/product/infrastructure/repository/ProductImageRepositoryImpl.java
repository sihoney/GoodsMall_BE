package com.example.product.infrastructure.repository;

import com.example.product.domain.entity.ProductImage;
import com.example.product.domain.repository.ProductImageRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * ProductImageRepository 구현체
 */
@Repository
@RequiredArgsConstructor
public class ProductImageRepositoryImpl implements ProductImageRepository {

    private final ProductImageJpaRepository jpaRepository;

    @Override
    public ProductImage save(ProductImage productImage) {
        return jpaRepository.save(productImage);
    }

    @Override
    public List<ProductImage> findByProductId(UUID productId) {
        return jpaRepository.findByProductId(productId);
    }

    @Override
    public Optional<ProductImage> findThumbnailByProductId(UUID productId) {
        return jpaRepository.findThumbnailByProductId(productId);
    }

    @Override
    public Optional<ProductImage> findById(UUID imageId) {
        return jpaRepository.findById(imageId);
    }

    @Override
    public void deleteById(UUID imageId) {
        jpaRepository.deleteById(imageId);
    }

    @Override
    public void deleteByProductId(UUID productId) {
        jpaRepository.deleteByProductId(productId);
    }
}
