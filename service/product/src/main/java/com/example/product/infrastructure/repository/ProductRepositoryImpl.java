package com.example.product.infrastructure.repository;

import com.example.product.domain.entity.Product;
import com.example.product.domain.entity.ProductImage;
import com.example.product.domain.enumtype.ProductStatus;
import com.example.product.domain.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository jpaRepository;
    private final ProductImageJpaRepository imageJpaRepository;

    @Override
    public Product save(Product product) {
        return jpaRepository.save(product);
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }

    @Override
    public Page<Product> findDisplayProductsWithFilters(
            List<UUID> categoryIds,
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    ) {
        return jpaRepository.findDisplayProductsWithFilters(categoryIds,
                                                            keyword,
                                                            minPrice,
                                                            maxPrice,
                                                            ProductStatus.ACTIVE,
                                                            pageable);
    }

    @Override
    public Page<Product> findPopularProducts(Pageable pageable) {
        return jpaRepository.findPopularProducts(pageable);
    }

    @Override
    public Page<Product> findBySellerId(UUID sellerId, Pageable pageable) {
        return jpaRepository.findBySellerId(sellerId, pageable);
    }

    @Override
    public boolean existsActiveBySellerId(UUID sellerId) {
        return jpaRepository.existsBySellerIdAndStatusAndDeletedAtIsNull(sellerId, ProductStatus.ACTIVE);
    }

    @Override
    public Optional<Product> findById(UUID productId) {
        return jpaRepository.findById(productId);
    }

    @Override
    public Optional<Product> findByIdWithLock(UUID productId) {
        return jpaRepository.findByIdWithLock(productId);
    }

    @Override
    public List<Product> findAllByProductIdIn(List<UUID> productIds) {
        return jpaRepository.findAllByProductIdIn(productIds);
    }

    @Override
    public Optional<ProductImage> findThumbnailImageByProductId(UUID productId) {
        return imageJpaRepository.findThumbnailByProductId(productId);
    }

    @Override
    public List<UUID> findIdsByUpdatedAtAfter(LocalDateTime since) {
        return jpaRepository.findIdsByUpdatedAtAfter(since);
    }
}
