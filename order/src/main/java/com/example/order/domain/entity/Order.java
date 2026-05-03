package com.example.order.domain.entity;

import com.example.order.domain.enumtype.OrderItemStatus;
import com.example.order.domain.enumtype.OrderStatus;
import com.example.order.domain.enumtype.OrderType;
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
//변경감지
@Getter
@Entity
@Table(name = "orders", schema = "order_service")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @Column(name = "order_id", nullable = false, updatable = false)
    private UUID orderId;

    @Column(name = "order_number", nullable = false, updatable = false, unique = true, length = 14)
    private String orderNumber;

    @Column(name = "buyer_id", nullable = false)
    private UUID buyerId;

    @DecimalMin("0.00")
    @Column(name = "total_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 30)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 20)
    private OrderType orderType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "address")
    private String address;

    @Column(name = "address_detail")
    private String addressDetail;

    @Column(name = "zip_code")
    private String zipCode;

    @Column(name = "receiver")
    private String receiver;

    @Column(name = "receiver_phone")
    private String receiverPhone;

    @Column(name = "representative_product_name", nullable = false)
    private String representativeProductName;

    @Column(name = "representative_thumbnail_key")
    private String representativeThumbnailKey;

    @Min(1)
    @Column(name = "item_count", nullable = false)
    private Integer itemCount;

    @Column(name = "auction_id")
    private UUID auctionId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private final List<OrderItem> items = new ArrayList<>();

    private Order(
            UUID orderId,
            String orderNumber,
            UUID buyerId,
            BigDecimal totalPrice,
            OrderStatus status,
            OrderType orderType,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String address,
            String addressDetail,
            String zipCode,
            String receiver,
            String receiverPhone,
            String representativeProductName,
            String representativeThumbnailKey,
            Integer itemCount,
            UUID auctionId
    ) {
        this.orderId = Objects.requireNonNull(orderId);
        this.orderNumber = Objects.requireNonNull(orderNumber);
        this.buyerId = Objects.requireNonNull(buyerId);
        this.totalPrice = Objects.requireNonNull(totalPrice);
        this.status = Objects.requireNonNull(status);
        this.orderType = Objects.requireNonNull(orderType);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.address = address;
        this.addressDetail = addressDetail;
        this.zipCode = zipCode;
        this.receiver = receiver;
        this.receiverPhone = receiverPhone;
        this.representativeProductName = Objects.requireNonNull(representativeProductName);
        this.representativeThumbnailKey = representativeThumbnailKey;
        this.itemCount = Objects.requireNonNull(itemCount);
        this.auctionId = auctionId;
    }

    public static Order create(
            String orderNumber,
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
                orderNumber,
                buyerId,
                BigDecimal.ZERO,
                OrderStatus.CREATED,
                OrderType.NORMAL,
                LocalDateTime.now(),
                LocalDateTime.now(),
                address,
                addressDetail,
                zipCode,
                receiver,
                receiverPhone,
                representativeProductName,
                representativeThumbnailKey,
                itemCount,
                null
        );
    }

    public static Order createForAuction(
            String orderNumber,
            UUID auctionId,
            UUID buyerId,
            String representativeProductName,
            String representativeThumbnailKey
    ) {
        return new Order(
                UUID.randomUUID(),
                orderNumber,
                buyerId,
                BigDecimal.ZERO,
                OrderStatus.CREATED,
                OrderType.AUCTION,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                null,
                null,
                null,
                null,
                representativeProductName,
                representativeThumbnailKey,
                1,
                Objects.requireNonNull(auctionId)
        );
    }

    public void assignShippingInfo(
            String address,
            String addressDetail,
            String zipCode,
            String receiver,
            String receiverPhone
    ) {
        this.address = Objects.requireNonNull(address);
        this.addressDetail = Objects.requireNonNull(addressDetail);
        this.zipCode = Objects.requireNonNull(zipCode);
        this.receiver = Objects.requireNonNull(receiver);
        this.receiverPhone = Objects.requireNonNull(receiverPhone);
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isShippingInfoAssigned() {
        return this.address != null;
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

    public void cancelByPaymentFailure() {
        this.status = OrderStatus.CANCELED;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel(boolean hasRemainingItems) {
        if (!hasRemainingItems) {
            this.status = OrderStatus.CANCELED;
            this.updatedAt = LocalDateTime.now();
            return;
        }
        if (isFlowState(this.status)) {
            recomputeFlowStatus();
            this.updatedAt = LocalDateTime.now();
            return;
        }
        this.status = OrderStatus.PARTIAL_CANCELED;
        this.updatedAt = LocalDateTime.now();
    }

    private boolean isFlowState(OrderStatus status) {
        return status == OrderStatus.SHIPPING
                || status == OrderStatus.PARTIAL_SHIPPING
                || status == OrderStatus.DELIVERED
                || status == OrderStatus.COMPLETED;
    }

    private void recomputeFlowStatus() {
        List<OrderItem> active = this.items.stream()
                .filter(item -> item.getStatus() != OrderItemStatus.CANCELED)
                .toList();
        if (active.isEmpty()) {
            return;
        }

        boolean allCompleted = active.stream()
                .allMatch(item -> item.getStatus() == OrderItemStatus.COMPLETED);
        if (allCompleted) {
            this.status = OrderStatus.COMPLETED;
            return;
        }

        boolean allDelivered = active.stream()
                .allMatch(item -> item.getStatus() == OrderItemStatus.DELIVERED
                        || item.getStatus() == OrderItemStatus.RETURN_REQUESTED
                        || item.getStatus() == OrderItemStatus.COMPLETED);
        if (allDelivered) {
            this.status = OrderStatus.DELIVERED;
            if (this.deliveredAt == null) {
                this.deliveredAt = LocalDateTime.now();
            }
            return;
        }

        boolean allShipping = active.stream()
                .allMatch(item -> item.getStatus() == OrderItemStatus.SHIPPING);
        this.status = allShipping ? OrderStatus.SHIPPING : OrderStatus.PARTIAL_SHIPPING;
    }

    public void markShipping() {
        boolean allShipping = this.items.stream()
                .filter(item -> item.getStatus() != OrderItemStatus.CANCELED)
                .allMatch(item -> item.getStatus() == OrderItemStatus.SHIPPING);

        this.status = allShipping ? OrderStatus.SHIPPING : OrderStatus.PARTIAL_SHIPPING;
        this.updatedAt = LocalDateTime.now();
    }

    public void markDelivered() {
        boolean allDelivered = this.items.stream()
                .filter(item -> item.getStatus() != OrderItemStatus.CANCELED)
                .allMatch(item -> item.getStatus() == OrderItemStatus.DELIVERED
                        || item.getStatus() == OrderItemStatus.RETURN_REQUESTED
                        || item.getStatus() == OrderItemStatus.COMPLETED);

        if (allDelivered) {
            this.status = OrderStatus.DELIVERED;
            this.deliveredAt = LocalDateTime.now();
        } else {
            this.status = OrderStatus.PARTIAL_SHIPPING;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void complete() {
        this.items.stream()
                .filter(item -> item.getStatus() != OrderItemStatus.CANCELED)
                .filter(item -> item.getStatus() != OrderItemStatus.RETURN_REQUESTED)
                .filter(item -> item.getStatus() != OrderItemStatus.COMPLETED)
                .forEach(OrderItem::complete);

        boolean allTerminal = this.items.stream()
                .allMatch(item -> item.getStatus() == OrderItemStatus.COMPLETED
                        || item.getStatus() == OrderItemStatus.CANCELED);
        if (allTerminal) {
            this.status = OrderStatus.COMPLETED;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void completeIfAllItemsCompleted() {
        boolean allCompleted = this.items.stream()
                .filter(item -> item.getStatus() != OrderItemStatus.CANCELED)
                .allMatch(item -> item.getStatus() == OrderItemStatus.COMPLETED);

        if (allCompleted) {
            this.status = OrderStatus.COMPLETED;
            this.updatedAt = LocalDateTime.now();
        }
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
