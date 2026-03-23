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
@Table(name = "wish")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wish {

    @Id
    @Column(name = "wish_id", nullable = false, updatable = false)
    private UUID wishId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    private Wish(UUID wishId, UUID productId, UUID memberId, LocalDateTime createdAt, LocalDateTime deletedAt) {
        this.wishId = Objects.requireNonNull(wishId);
        this.productId = Objects.requireNonNull(productId);
        this.memberId = Objects.requireNonNull(memberId);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.deletedAt = deletedAt;
    }

    public static Wish create(UUID wishId, UUID productId, UUID memberId, LocalDateTime createdAt) {
        return new Wish(wishId, productId, memberId, createdAt, null);
    }

    public void delete(LocalDateTime deletedAt) {
        this.deletedAt = Objects.requireNonNull(deletedAt);
    }
}
