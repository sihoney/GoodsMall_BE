package com.example.product.domain.entity;

import com.example.product.domain.enumtype.ProductStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProductStatus status;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 전체 필드 생성자 (테스트 또는 특수 상황용)
     */
    private Product(
        String sellerId,
        String title,
        String description,
        BigDecimal price,
        Integer stock_quantity,
        Category category   // 추가
    ) {
        LocalDateTime now = LocalDateTime.now();
        this.productId = UUID.randomUUID();
        this.sellerId = UUID.fromString(sellerId);
        this.title = Objects.requireNonNull(title);
        this.description = description;
        this.price = Objects.requireNonNull(price);
        this.stockQuantity = stock_quantity;
        this.category = Objects.requireNonNull(category);
        this.status = ProductStatus.ACTIVE;
        this.viewCount = 0;
        this.createdAt = now;
        this.updatedAt = now;
        this.deletedAt = null;
    }

    public static Product create(
        String sellerId,
        String title,
        String description,
        BigDecimal price,
        Integer count,
        Category category
    ) {
        return new Product(sellerId, title, description, price, count, category);
    }
}
