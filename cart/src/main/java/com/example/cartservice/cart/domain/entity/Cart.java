package com.example.cartservice.cart.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @OneToMany(mappedBy = "cart", fetch = FetchType.LAZY)
    private List<CartItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private Cart(UUID memberId) {
        LocalDateTime now = LocalDateTime.now();
        this.id = UUID.randomUUID();
        validateMemberId(memberId);
        this.memberId = memberId;
        this.items = new ArrayList<>();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static Cart create(UUID memberId) {
        return new Cart(memberId);
    }

    public void addItem(CartItem item) {
        validateItem(item);
        this.items.add(item);
        updateTimestamp();
    }

    public void removeItem(CartItem item) {
        validateItem(item);
        this.items.remove(item);
        updateTimestamp();
    }

    public void clearItems() {
        this.items.clear();
        updateTimestamp();
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    public int getItemCount() {
        return this.items.size();
    }

    private void validateMemberId(UUID memberId) {
        Objects.requireNonNull(memberId, "회원 ID는 필수입니다");
    }

    private void validateItem(CartItem item) {
        Objects.requireNonNull(item, "장바구니 항목은 필수입니다");
    }

    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}
