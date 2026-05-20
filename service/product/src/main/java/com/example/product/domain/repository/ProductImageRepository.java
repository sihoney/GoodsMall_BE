package com.example.product.domain.repository;

import com.example.product.domain.entity.ProductImage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductImageRepository {

    ProductImage save(ProductImage productImage);

    List<ProductImage> findByProductId(UUID productId);

    Optional<ProductImage> findThumbnailByProductId(UUID productId);

    Optional<ProductImage> findById(UUID imageId);

    void deleteById(UUID imageId);

    void deleteByProductId(UUID productId);
}
