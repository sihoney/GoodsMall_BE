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

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "order_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @Column(name = "order_item_id", nullable = false, updatable = false)
    private UUID orderItemId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "product_name_snapshot")
    private String productNameSnapshot;

    @Column(name = "unit_price_snapshot")
    private BigDecimal unitPriceSnapshot;

    @Column(name = "quantity")
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderItemStatus status;

    @Column(name = "thumbnail_key_snapshot")
    private String thumbnailKeySnapshot;

    private OrderItem(
            UUID orderItemId,
            UUID productId,
            Order order,
            UUID sellerId,
            String productNameSnapshot,
            BigDecimal unitPriceSnapshot,
            Integer quantity,
            OrderItemStatus status,
            String thumbnailKeySnapshot
    ) {
        this.orderItemId = Objects.requireNonNull(orderItemId);
        this.productId = Objects.requireNonNull(productId);
        this.order = Objects.requireNonNull(order);
        this.sellerId = Objects.requireNonNull(sellerId);
        this.productNameSnapshot = productNameSnapshot;
        this.unitPriceSnapshot = unitPriceSnapshot;
        this.quantity = quantity;
        this.status = status;
        this.thumbnailKeySnapshot = thumbnailKeySnapshot;
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
                thumbnailKeySnapshot
        );
    }

    public BigDecimal getTotalPrice(BigDecimal unitPriceSnapshot, int quantity) {
        return unitPriceSnapshot.multiply(BigDecimal.valueOf(quantity));
    }

    public void changeStatus(OrderItemStatus status) {
        this.status = status;
    }
}
