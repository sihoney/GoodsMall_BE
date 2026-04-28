package com.example.product.domain.entity;

import com.example.product.common.exception.ProductAlreadyDeletedException;
import com.example.product.common.exception.SellerNotAuthorizedException;
import com.example.product.domain.enumtype.ProductStatus;
import com.example.product.domain.enumtype.ProductType;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ProductType type;

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
            Category category,
            ProductType type
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
        this.type = type != null ? type : ProductType.GENERAL;
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
            Category category,
            ProductType type
    ) {
        return new Product(sellerId, title, description, price, count, category, type);
    }

    public void validateSeller(UUID requestSellerId) {
        if (!this.sellerId.equals(requestSellerId)) {
            throw new SellerNotAuthorizedException();
        }
    }


    public void updateProductInfo(String title, String description, BigDecimal price) {
        validateTitle(title);
        this.title = title;
        this.description = description;
        this.price = price;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateCategory(Category newCategory) {
        this.category = Objects.requireNonNull(newCategory, "카테고리는 필수입니다");
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStock(Integer newStock) {
        this.stockQuantity = newStock;
        validateSoldOut(newStock);
        validateActive(newStock);
        this.updatedAt = LocalDateTime.now();
    }

    private void validateActive(Integer newStock) {
        if (newStock > 0 && this.status == ProductStatus.SOLD_OUT) {
            this.status = ProductStatus.ACTIVE;
        }
    }

    private void validateSoldOut(Integer newStock) {
        if (newStock == 0 && this.status == ProductStatus.ACTIVE) {
            this.status = ProductStatus.SOLD_OUT;
        }
    }

    public void increaseStock(Integer quantity) {
        int newStock = this.stockQuantity + quantity;
        updateStock(newStock);
    }

    public void decreaseStock(Integer quantity) {
        validateEnoughStock(quantity);
        updateStock(this.stockQuantity - quantity);
    }

    private void validateEnoughStock(Integer quantity) {
        if (this.stockQuantity < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다");
        }
    }

    public void updateStatus(ProductStatus newStatus) {
        Objects.requireNonNull(newStatus, "상태는 필수입니다");

        if (newStatus == ProductStatus.SOLD_OUT && this.stockQuantity > 0) {
            throw new IllegalArgumentException("재고가 남아있는 상품은 품절 상태로 변경할 수 없습니다");
        }

        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public void delete() {
        if (this.deletedAt != null) {
            throw new ProductAlreadyDeletedException();
        }
        this.status = ProductStatus.INACTIVE;
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void restore() {
        if (this.deletedAt == null) {
            throw new IllegalStateException("삭제되지 않은 상품은 복구할 수 없습니다");
        }
        this.deletedAt = null;
        this.status = ProductStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void increaseViewCount() {
        this.viewCount++;
    }


    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public boolean isActive() {
        return this.status == ProductStatus.ACTIVE && !isDeleted();
    }

    // 도메인 규칙: 상품명 길이 제한
    private void validateTitle(String title) {
        if (title.length() > 255) {
            throw new IllegalArgumentException("상품명은 255자를 초과할 수 없습니다");
        }
    }
}
