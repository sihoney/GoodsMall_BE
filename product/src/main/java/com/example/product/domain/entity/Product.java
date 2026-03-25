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

    /**
     * 전체 필드 생성자 (테스트 또는 특수 상황용)
     */
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

    /**
     * 신규 등록용 생성자 (create 메서드용)
     * productId, status, viewCount, createdAt, updatedAt 자동 생성
     */
    private Product(
        String sellerId,
        String title,
        String description,
        BigDecimal price,
        Integer count
    ) {
        LocalDateTime now = LocalDateTime.now();
        this.productId = UUID.randomUUID();
        this.sellerId = UUID.fromString(sellerId);
        this.title = Objects.requireNonNull(title);
        this.description = description;
        this.price = Objects.requireNonNull(price);
        this.count = count;
        this.status = ProductStatus.ACTIVE;
        this.viewCount = 0;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * 상품 생성 (신규 등록용)
     * productId, status(ACTIVE), viewCount, createdAt, updatedAt는 자동 생성
     *
     * @param sellerId    판매자 ID (String)
     * @param title       상품명
     * @param description 상품 설명
     * @param price       가격
     * @param count       재고
     * @return 생성된 Product
     */
    public static Product create(
        String sellerId,
        String title,
        String description,
        BigDecimal price,
        Integer count
    ) {
        return new Product(sellerId, title, description, price, count);
    }
}
