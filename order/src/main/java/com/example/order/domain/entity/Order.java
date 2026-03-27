package com.example.order.domain.entity;

import com.example.order.domain.enumtype.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
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
@Table(name = "\"order\"")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @Column(name = "order_id", nullable = false, updatable = false)
    private UUID orderId;

    @Column(name = "buyer_id", nullable = false)
    private UUID buyerId;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "address")
    private String address;

    @Column(name = "address_detail")
    private String addressDetail;

    @Column(name = "\"zipCode\"")
    private String zipCode;

    @Column(name = "receiver")
    private String receiver;

    @Column(name = "receiver_phone")
    private String receiverPhone;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private final List<OrderItem> items = new ArrayList<>();

    private Order(
            UUID orderId,
            UUID buyerId,
            BigDecimal totalPrice,
            OrderStatus orderStatus,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String address,
            String addressDetail,
            String zipCode,
            String receiver,
            String receiverPhone
    ) {
        this.orderId = Objects.requireNonNull(orderId);
        this.buyerId = Objects.requireNonNull(buyerId);
        this.totalPrice = Objects.requireNonNull(totalPrice);
        this.orderStatus = Objects.requireNonNull(orderStatus);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.address = address;
        this.addressDetail = addressDetail;
        this.zipCode = zipCode;
        this.receiver = receiver;
        this.receiverPhone = receiverPhone;
    }

    public static Order create(
            UUID buyerId,
            String address,
            String addressDetail,
            String zipCode,
            String receiver,
            String receiverPhone
    ) {
        return new Order(
                UUID.randomUUID(),
                buyerId,
                BigDecimal.ZERO,
                OrderStatus.PENDING_PAYMENT,
                LocalDateTime.now(),
                LocalDateTime.now(),
                address,
                addressDetail,
                zipCode,
                receiver,
                receiverPhone
        );
    }

    public void changeStatus(OrderStatus orderStatus, LocalDateTime updatedAt) {
        this.orderStatus = Objects.requireNonNull(orderStatus);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public void updateDeliveryAddress(
            String address,
            String addressDetail,
            String zipCode,
            String receiver,
            String receiverPhone,
            LocalDateTime updatedAt
    ) {
        this.address = address;
        this.addressDetail = addressDetail;
        this.zipCode = zipCode;
        this.receiver = receiver;
        this.receiverPhone = receiverPhone;
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }
}
