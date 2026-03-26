package com.example.product.domain.entity;

import com.example.product.domain.enumtype.ProductStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "count")
    private Integer count;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProductStatus status;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private Product(
            UUID productId,
            UUID sellerId,
            String title,
            String description,
            BigDecimal price,
            Integer count,
            ProductStatus status,
            Integer viewCount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.productId = Objects.requireNonNull(productId);
        this.sellerId = Objects.requireNonNull(sellerId);
        this.title = Objects.requireNonNull(title);
        this.description = description;
        this.price = Objects.requireNonNull(price);
        this.count = count;
        this.status = Objects.requireNonNull(status);
        this.viewCount = Objects.requireNonNull(viewCount);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Product create(
            UUID productId,
            UUID sellerId,
            String title,
            String description,
            BigDecimal price,
            Integer count,
            ProductStatus status,
            Integer viewCount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new Product(productId, sellerId, title, description, price, count, status, viewCount, createdAt, updatedAt);
    }

    public void changeStatus(ProductStatus status, LocalDateTime updatedAt) {
        this.status = Objects.requireNonNull(status);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public void increaseViewCount() {
        this.viewCount = this.viewCount + 1;
    }

    public void updateDetails(String title, String description, BigDecimal price, Integer count, LocalDateTime updatedAt) {
        this.title = Objects.requireNonNull(title);
        this.description = description;
        this.price = Objects.requireNonNull(price);
        this.count = count;
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }
}
