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
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @Column(name = "order_id", nullable = false, updatable = false)
    private UUID orderId;

    @Column(name = "buyer_id", nullable = false)
    private UUID buyerId;

    @DecimalMin("0.00")
    @Column(name = "total_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 30)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "address_detail", nullable = false)
    private String addressDetail;

    @Column(name = "zip_code", nullable = false)
    private String zipCode;

    @Column(name = "receiver", nullable = false)
    private String receiver;

    @Column(name = "receiver_phone", nullable = false)
    private String receiverPhone;

    @Column(name = "representative_product_name", nullable = false)
    private String representativeProductName;

    @Column(name = "representative_thumbnail_key")
    private String representativeThumbnailKey;

    @Min(1)
    @Column(name = "item_count", nullable = false)
    private Integer itemCount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private final List<OrderItem> items = new ArrayList<>();

    private Order(
            UUID orderId,
            UUID buyerId,
            BigDecimal totalPrice,
            OrderStatus status,
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
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.address = Objects.requireNonNull(address);
        this.addressDetail = Objects.requireNonNull(addressDetail);
        this.zipCode = Objects.requireNonNull(zipCode);
        this.receiver = Objects.requireNonNull(receiver);
        this.receiverPhone = Objects.requireNonNull(receiverPhone);
        this.representativeProductName = Objects.requireNonNull(representativeProductName);
        this.representativeThumbnailKey = representativeThumbnailKey;
        this.itemCount = Objects.requireNonNull(itemCount);
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

    public boolean confirm() {
        if (this.status == OrderStatus.CONFIRMED) {
            return false;
        }
        if (this.status == OrderStatus.CANCELED) {
            return false;
        }

        this.status = OrderStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
        return true;
    }
}
