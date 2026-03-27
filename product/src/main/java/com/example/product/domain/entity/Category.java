package com.example.product.domain.entity;

import com.example.product.common.exception.CategoryAlreadyDeletedException;
import com.example.product.common.exception.CategoryDepthExceededException;
import com.example.product.domain.enumtype.CategoryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @Column(name = "category_id", nullable = false, updatable = false)
    private UUID categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @Column(name = "seller_id")
    private UUID sellerId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "depth", nullable = false)
    private Integer depth;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CategoryStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    private Category(
            Category parent,
            UUID sellerId,
            String name,
            String description,
            Integer depth,
            Integer sortOrder
    ) {
        LocalDateTime now = LocalDateTime.now();
        this.categoryId = UUID.randomUUID();
        this.parent = parent;
        this.sellerId = sellerId;
        this.name = Objects.requireNonNull(name);
        this.description = description;
        this.depth = Objects.requireNonNull(depth);
        this.sortOrder = Objects.requireNonNull(sortOrder);
        this.status = CategoryStatus.ACTIVE;
        this.createdAt = now;
        this.updatedAt = now;
        this.deletedAt = null;
    }


    public static Category createRoot(
            String name,
            String description,
            Integer sortOrder
    ) {
        return new Category(null, null, name, description, 0, sortOrder);
    }

    public static Category createChild(
            Category parent,
            UUID sellerId,
            String name,
            String description,
            Integer sortOrder
    ) {
        Objects.requireNonNull(parent, "부모 카테고리는 필수입니다");
        Objects.requireNonNull(sellerId, "판매자 ID는 필수입니다");

        if (parent.depth >= 2) {
            throw new CategoryDepthExceededException();
        }

        if (parent.depth == 0 && parent.sellerId != null) {
            throw new IllegalArgumentException("관리자가 생성한 대분류에만 하위 카테고리를 생성할 수 있습니다");
        }

        return new Category(parent, sellerId, name, description, parent.getDepth() + 1, sortOrder);
    }

    public void update(
            String name,
            String description,
            Integer sortOrder
    ) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("카테고리명은 비어있을 수 없습니다");
        }
        this.name = name;
        this.description = description;
        this.sortOrder = Objects.requireNonNull(sortOrder);
        this.updatedAt = LocalDateTime.now();
    }

    public void delete() {
        if (this.deletedAt != null) {
            throw new CategoryAlreadyDeletedException();
        }
        this.status = CategoryStatus.INACTIVE;
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void validateSeller(UUID requestSellerId) {
        if (this.sellerId == null) {
            throw new IllegalStateException("관리자가 관리하는 카고테리는 수정할 수 없습니다");
        }
        if (!this.sellerId.equals(requestSellerId)) {
            throw new IllegalStateException("해당 카테고리에 대한 권한이 없습니다");
        }
    }
}

