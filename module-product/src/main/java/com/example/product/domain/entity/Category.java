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
@Table(name = "category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @Column(name = "category_id", nullable = false, updatable = false)
    private UUID categoryId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    private Category(UUID categoryId, String name, LocalDateTime createdAt, UUID productId) {
        this.categoryId = Objects.requireNonNull(categoryId);
        this.name = Objects.requireNonNull(name);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.productId = Objects.requireNonNull(productId);
    }

    public static Category create(UUID categoryId, String name, LocalDateTime createdAt, UUID productId) {
        return new Category(categoryId, name, createdAt, productId);
    }

    public void rename(String name) {
        this.name = Objects.requireNonNull(name);
    }
}
