package com.example.product.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "product_image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductImage {

    @Id
    @Column(name = "image_id", nullable = false, updatable = false)
    private UUID imageId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "s3_key", nullable = false)
    private String s3Key;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "is_thumbnail", nullable = false)
    private boolean thumbnail;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private ProductImage(
            UUID imageId,
            UUID productId,
            String s3Key,
            Integer sortOrder,
            boolean thumbnail,
            LocalDateTime createdAt
    ) {
        this.imageId = Objects.requireNonNull(imageId);
        this.productId = Objects.requireNonNull(productId);
        this.s3Key = Objects.requireNonNull(s3Key);
        this.sortOrder = Objects.requireNonNull(sortOrder);
        this.thumbnail = thumbnail;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static ProductImage create(
            UUID imageId,
            UUID productId,
            String s3Key,
            Integer sortOrder,
            boolean thumbnail,
            LocalDateTime createdAt
    ) {
        return new ProductImage(imageId, productId, s3Key, sortOrder, thumbnail, createdAt);
    }

    public void changeSortOrder(Integer sortOrder) {
        this.sortOrder = Objects.requireNonNull(sortOrder);
    }

    public void markAsThumbnail() {
        this.thumbnail = true;
    }

    public void unmarkThumbnail() {
        this.thumbnail = false;
    }
}
