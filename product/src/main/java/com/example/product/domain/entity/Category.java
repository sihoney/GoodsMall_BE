package com.example.product.domain.entity;

import com.example.product.common.exception.CategoryDepthExceededException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "depth", nullable = false)
    private Integer depth;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Category(
        Category category,
        String name,
        Integer depth,
        Integer sortOrder
    ) {
        this.categoryId = UUID.randomUUID();
        this.parent = category;
        this.name = Objects.requireNonNull(name);
        this.depth = Objects.requireNonNull(depth);
        this.sortOrder = Objects.requireNonNull(sortOrder);
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 대분류 생성 (depth = 0, parentId = null)
     */
    public static Category createRoot(String name, Integer sortOrder) {
        return new Category(null, name, 0, sortOrder);
    }

    /**
     * 하위 분류 생성 (중분류, 소분류) 부모의 depth + 1로 자동 설정
     */
    public static Category createChild(Category parent, String name, Integer sortOrder) {
        Objects.requireNonNull(parent);
        if (parent.depth >= 2) {
            throw new CategoryDepthExceededException();
        }
        return new Category(parent, name, parent.getDepth() + 1, sortOrder);
    }

    public void changeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("카테고리명은 비어있을 수 없습니다");
        }
        this.name = name;
    }

    public void changeSortOrder(Integer sortOrder) {
        this.sortOrder = Objects.requireNonNull(sortOrder);
    }

    public boolean isRoot() {
        return this.parent == null;
    }
}
