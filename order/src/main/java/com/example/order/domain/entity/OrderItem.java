package com.example.order.domain.entity;

import com.example.order.domain.enumtype.OrderItemStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Entity
@Table(name = "order_items", schema = "order_service")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @Column(name = "order_item_id", nullable = false, updatable = false)
    private UUID orderItemId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "product_name_snapshot", nullable = false, length = 255)
    private String productNameSnapshot;

    @Column(name = "unit_price_snapshot", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPriceSnapshot;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_item_status", nullable = false, length = 30)
    private OrderItemStatus status;

    @Column(name = "thumbnail_key_snapshot", length = 255)
    private String thumbnailKeySnapshot;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private OrderItem(
            UUID orderItemId,
            UUID productId,
            Order order,
            UUID sellerId,
            String productNameSnapshot,
            BigDecimal unitPriceSnapshot,
            Integer quantity,
            OrderItemStatus status,
            String thumbnailKeySnapshot,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.orderItemId = Objects.requireNonNull(orderItemId);
        this.productId = Objects.requireNonNull(productId);
        this.order = Objects.requireNonNull(order);
        this.sellerId = Objects.requireNonNull(sellerId);
        this.productNameSnapshot = Objects.requireNonNull(productNameSnapshot);
        this.unitPriceSnapshot = Objects.requireNonNull(unitPriceSnapshot);
        this.quantity = Objects.requireNonNull(quantity);
        this.status = Objects.requireNonNull(status);
        this.thumbnailKeySnapshot = thumbnailKeySnapshot;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static OrderItem create(
            UUID productId,
            Order order,
            UUID sellerId,
            String productNameSnapshot,
            BigDecimal unitPriceSnapshot,
            Integer quantity,
            String thumbnailKeySnapshot
    ) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("수량은 0개보다 많아야 합니다.");
        }

        if (unitPriceSnapshot == null || unitPriceSnapshot.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("가격이 0원보다 높아야 합니다.");
        }

        return new OrderItem(
                UUID.randomUUID(),
                productId,
                order,
                sellerId,
                productNameSnapshot,
                unitPriceSnapshot,
                quantity,
                OrderItemStatus.PENDING,
                thumbnailKeySnapshot,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    public BigDecimal getTotalPrice(BigDecimal unitPriceSnapshot, int quantity) {
        return unitPriceSnapshot.multiply(BigDecimal.valueOf(quantity));
    }

    public void startShip() {
        if (this.status != OrderItemStatus.PREPARING) {
            throw new IllegalStateException("배송 시작은 상품 준비 중 상태에서만 가능합니다.");
        }
        this.status = OrderItemStatus.SHIPPING;
        this.updatedAt = LocalDateTime.now();
    }

    public void deliver() {
        if (this.status != OrderItemStatus.SHIPPING) {
            throw new IllegalStateException("배송 완료는 배송 중 상태에서만 가능합니다.");
        }
        this.status = OrderItemStatus.DELIVERED;
        this.updatedAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = OrderItemStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean cancel() {
        if (this.status.equals(OrderItemStatus.PENDING)) {
            this.status = OrderItemStatus.CANCELED;
            return true;
        }

        if (this.status.equals(OrderItemStatus.PREPARING)) {
            this.status = OrderItemStatus.CANCELED;
            return true;
        }

        return false;
    }
}
