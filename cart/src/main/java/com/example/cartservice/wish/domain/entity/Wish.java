package com.example.cartservice.wish.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "wish",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_wish_member_product", columnNames = {"member_id", "product_id"})
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wish {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Wish(UUID memberId, UUID productId) {
        this.id = UUID.randomUUID();
        validateConstructorParams(memberId, productId);
        this.memberId = memberId;
        this.productId = productId;
        this.createdAt = LocalDateTime.now();
    }

    public static Wish create(UUID memberId, UUID productId) {
        return new Wish(memberId, productId);
    }

    private void validateConstructorParams(UUID memberId, UUID productId) {
        Objects.requireNonNull(memberId, "회원 ID는 필수입니다");
        Objects.requireNonNull(productId, "상품 ID는 필수입니다");
    }
}
