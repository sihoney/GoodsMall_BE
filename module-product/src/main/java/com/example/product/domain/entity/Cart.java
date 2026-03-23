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
@Table(name = "cart")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart {

    @Id
    @Column(name = "cart_id", nullable = false, updatable = false)
    private UUID cartId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "count")
    private Integer count;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private Cart(UUID cartId, UUID productId, UUID memberId, Integer count, LocalDateTime createdAt) {
        this.cartId = Objects.requireNonNull(cartId);
        this.productId = Objects.requireNonNull(productId);
        this.memberId = Objects.requireNonNull(memberId);
        this.count = count;
        this.createdAt = createdAt;
    }

    public static Cart create(UUID cartId, UUID productId, UUID memberId, Integer count, LocalDateTime createdAt) {
        return new Cart(cartId, productId, memberId, count, createdAt);
    }

    public void changeCount(Integer count) {
        this.count = count;
    }
}
