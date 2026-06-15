package com.example.cartservice.cart.domain.entity;

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
//변경감지
@Getter
@Entity
@Table(name = "cart")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart {

    @Id
    @Column(name = "cart_id", nullable = false, updatable = false)
    private UUID cartId;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;

    private Cart(UUID memberId, UUID productId, Integer quantity) {
        this.cartId = UUID.randomUUID();
        validateConstructorParams(memberId, productId, quantity);
        this.memberId = memberId;
        this.productId = productId;
        this.quantity = quantity;
        this.addedAt = LocalDateTime.now();
    }

    public static Cart create(UUID memberId, UUID productId, Integer quantity) {
        return new Cart(memberId, productId, quantity);
    }

    public void updateQuantity(Integer newQuantity) {
        validateQuantity(newQuantity);
        this.quantity = newQuantity;
    }

    public void validateOwner(UUID requestMemberId) {
        if (!this.memberId.equals(requestMemberId)) {
            throw new IllegalArgumentException("장바구니 소유자만 수정할 수 있습니다");
        }
    }

    private void validateConstructorParams(UUID memberId, UUID productId, Integer quantity) {
        Objects.requireNonNull(memberId, "회원 ID는 필수입니다");
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
}
