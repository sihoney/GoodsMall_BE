package com.example.product.infrastructure.repository;

import com.example.product.domain.entity.Product;
import com.example.product.domain.enumtype.ProductStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductJpaRepository extends JpaRepository<Product, UUID> {

    @Query("""
        SELECT p
        FROM Product p
        WHERE p.status = :status
          AND p.deletedAt IS NULL
          AND (:categoryIds IS NULL OR p.category.categoryId IN :categoryIds)
          AND (COALESCE(:keyword, '') = ''
              OR p.title LIKE CONCAT('%', :keyword, '%')
              OR p.description LIKE CONCAT('%', :keyword, '%'))
          AND (:minPrice IS NULL OR p.price >= :minPrice)
          AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        """)
    Page<Product> findDisplayProductsWithFilters(
            @Param("categoryIds") List<UUID> categoryIds,
            @Param("keyword") String keyword,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("status") ProductStatus status,
            Pageable pageable
    );

    @Query("""
        SELECT p
        FROM Product p
        WHERE p.status = 'ACTIVE'
          AND p.deletedAt IS NULL
        ORDER BY p.viewCount DESC, p.createdAt DESC
        """)
    Page<Product> findPopularProducts(Pageable pageable);

    Page<Product> findBySellerId(UUID sellerId, Pageable pageable);

    Optional<Product> findById(UUID productId);

    List<Product> findAllByProductIdIn(List<UUID> productIds);
}
