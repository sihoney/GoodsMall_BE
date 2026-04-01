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

    @Column(name = "representative_product_name")
    private String representativeProductName;

    @Column(name = "representative_thumbnail_key")
    private String representativeThumbnailKey;

    @Column(name = "item_count")
    private Integer itemCount;

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
            String receiverPhone,
            String representativeProductName,
            String representativeThumbnailKey,
            Integer itemCount
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
        this.representativeProductName = representativeProductName;
        this.representativeThumbnailKey = representativeThumbnailKey;
        this.itemCount = itemCount;
    }

    public static Order create(
            UUID buyerId,
            String address,
            String addressDetail,
            String zipCode,
            String receiver,
            String receiverPhone,
            String representativeProductName,
            String representativeThumbnailKey,
            Integer itemCount
    ) {
        return new Order(
                UUID.randomUUID(),
                buyerId,
                BigDecimal.ZERO,
                OrderStatus.CREATED,
                LocalDateTime.now(),
                LocalDateTime.now(),
                address,
                addressDetail,
                zipCode,
                receiver,
                receiverPhone,
                representativeProductName,
                representativeThumbnailKey,
                itemCount
        );
    }

    public void addItem(
            UUID productId,
            UUID sellerId,
            String productName,
            BigDecimal unitPrice,
            Integer quantity,
            String thumbnailKeySnapshot
    ) {
        OrderItem orderItem = OrderItem.create(
                productId,
                this,
                sellerId,
                productName,
                unitPrice,
                quantity,
                thumbnailKeySnapshot);
        this.items.add(orderItem);
        this.totalPrice = this.totalPrice.add(
                unitPrice.multiply(BigDecimal.valueOf(quantity))
        );
    }

    public void confirm() {
        if (this.orderStatus == OrderStatus.CONFIRMED) {
            return;
        }
        if (this.orderStatus == OrderStatus.CANCELED) {
            return;
        }
        this.orderStatus = OrderStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }
}
