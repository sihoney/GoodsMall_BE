package com.example.order.domain.entity;

import com.example.order.domain.enumtype.OrderItemStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "product_name_snapshot")
    private String productNameSnapshot;

    @Column(name = "unit_price_snapshot")
    private Integer unitPriceSnapshot;

    @Column(name = "quantity")
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderItemStatus status;

    private OrderItem(
            UUID orderItemId,
            UUID productId,
            UUID orderId,
            UUID sellerId,
            String productNameSnapshot,
            Integer unitPriceSnapshot,
            Integer quantity,
            OrderItemStatus status
    ) {
        this.orderItemId = Objects.requireNonNull(orderItemId);
        this.productId = Objects.requireNonNull(productId);
        this.orderId = Objects.requireNonNull(orderId);
        this.sellerId = Objects.requireNonNull(sellerId);
        this.productNameSnapshot = productNameSnapshot;
        this.unitPriceSnapshot = unitPriceSnapshot;
        this.quantity = quantity;
        this.status = status;
    }

    public static OrderItem create(
            UUID orderItemId,
            UUID productId,
            UUID orderId,
            UUID sellerId,
            String productNameSnapshot,
            Integer unitPriceSnapshot,
            Integer quantity,
            OrderItemStatus status
    ) {
        return new OrderItem(
                orderItemId,
                productId,
                orderId,
                sellerId,
                productNameSnapshot,
                unitPriceSnapshot,
                quantity,
                status
        );
    }

    public void changeStatus(OrderItemStatus status) {
        this.status = status;
    }
}
