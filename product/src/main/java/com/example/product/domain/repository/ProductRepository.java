package com.example.product.domain.repository;

import com.example.product.domain.entity.Product;
import com.example.product.domain.entity.ProductImage;
import com.example.product.domain.enumtype.ProductStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository {

    Product save(Product product);

    Page<Product> findAll(Pageable pageable);

    Page<Product> findDisplayProductsWithFilters(
            List<UUID> categoryIds,
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    );

    Page<Product> findPopularProducts(Pageable pageable);

    Page<Product> findBySellerId(UUID sellerId, Pageable pageable);

    Optional<Product> findById(UUID productId);

    Optional<ProductImage> findThumbnailImageByProductId(UUID productId);
}
