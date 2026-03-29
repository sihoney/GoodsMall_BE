package com.example.cartservice.cart.domain.entity;

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
@Table(name = "cart_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private CartItem(Cart cart, UUID productId, Integer quantity) {
        LocalDateTime now = LocalDateTime.now();
        this.id = UUID.randomUUID();
        validateConstructorParams(cart, productId, quantity);
        this.cart = cart;
        this.productId = productId;
        this.quantity = quantity;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static CartItem create(Cart cart, UUID productId, Integer quantity) {
        return new CartItem(cart, productId, quantity);
    }

    public void updateQuantity(Integer newQuantity) {
        validateQuantity(newQuantity);
        this.quantity = newQuantity;
        updateTimestamp();
    }

    private void validateConstructorParams(Cart cart, UUID productId, Integer quantity) {
        Objects.requireNonNull(cart, "장바구니는 필수입니다");
        Objects.requireNonNull(productId, "상품 ID는 필수입니다");
        validateQuantity(quantity);
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException("수량은 필수입니다");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("수량은 최소 1개 이상이어야 합니다");
        }
    }

    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}
