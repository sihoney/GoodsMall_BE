package com.example.order.domain.entity;

import com.example.order.domain.enumtype.DeliveryStatus;
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

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Entity
@Table(name = "delivery")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery {

    @Id
    @Column(name = "delivery_id", nullable = false, updatable = false)
    private UUID deliveryId;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "buyer_id", nullable = false)
    private UUID buyerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(name = "courier_code", length = 50)
    private String courierCode;

    @Column(name = "invoice_number", length = 100)
    private String invoiceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false, length = 30)
    private DeliveryStatus status;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private Delivery(
            UUID deliveryId,
            UUID sellerId,
            UUID buyerId,
            OrderItem orderItem,
            String courierCode,
            String invoiceNumber,
            DeliveryStatus status,
            LocalDateTime shippedAt,
            LocalDateTime deliveredAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.deliveryId = Objects.requireNonNull(deliveryId);
        this.sellerId = Objects.requireNonNull(sellerId);
        this.buyerId = Objects.requireNonNull(buyerId);
        this.orderItem = Objects.requireNonNull(orderItem);
        this.courierCode = courierCode;
        this.invoiceNumber = invoiceNumber;
        this.status = Objects.requireNonNull(status);
        this.shippedAt = shippedAt;
        this.deliveredAt = deliveredAt;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Delivery create(
            UUID sellerId,
            UUID buyerId,
            OrderItem orderItem
    ) {
        return new Delivery(
                UUID.randomUUID(),
                sellerId,
                buyerId,
                orderItem,
                null,
                null,
                DeliveryStatus.PREPARING,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
