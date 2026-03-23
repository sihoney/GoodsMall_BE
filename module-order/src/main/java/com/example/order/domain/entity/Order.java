package com.example.order.domain.entity;

import com.example.order.domain.enumtype.OrderStatus;
import com.example.order.domain.enumtype.TradeMethod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Column(name = "totalprice", nullable = false)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_method", nullable = false)
    private TradeMethod tradeMethod;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "address")
    private String address;

    @Column(name = "address_detail")
    private String addressDetail;

    @Column(name = "\"zipCode\"")
    private Integer zipCode;

    @Column(name = "receiver")
    private String receiver;

    @Column(name = "receiver_phone")
    private String receiverPhone;

    private Order(
            UUID orderId,
            UUID buyerId,
            BigDecimal totalPrice,
            OrderStatus orderStatus,
            TradeMethod tradeMethod,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String address,
            String addressDetail,
            Integer zipCode,
            String receiver,
            String receiverPhone
    ) {
        this.orderId = Objects.requireNonNull(orderId);
        this.buyerId = Objects.requireNonNull(buyerId);
        this.totalPrice = Objects.requireNonNull(totalPrice);
        this.orderStatus = Objects.requireNonNull(orderStatus);
        this.tradeMethod = Objects.requireNonNull(tradeMethod);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.address = address;
        this.addressDetail = addressDetail;
        this.zipCode = zipCode;
        this.receiver = receiver;
        this.receiverPhone = receiverPhone;
    }

    public static Order create(
            UUID orderId,
            UUID buyerId,
            BigDecimal totalPrice,
            OrderStatus orderStatus,
            TradeMethod tradeMethod,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String address,
            String addressDetail,
            Integer zipCode,
            String receiver,
            String receiverPhone
    ) {
        return new Order(
                orderId,
                buyerId,
                totalPrice,
                orderStatus,
                tradeMethod,
                createdAt,
                updatedAt,
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
            Integer zipCode,
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
