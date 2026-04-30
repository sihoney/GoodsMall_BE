package com.example.order.presentation.dto.response;

import com.example.order.domain.entity.Claim;
import com.example.order.domain.entity.OrderItem;
import com.example.order.domain.entity.ReturnRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReturnRequestSummaryResponse(
        UUID returnRequestId,
        UUID orderId,
        UUID orderItemId,
        UUID buyerId,
        String productName,
        String thumbnailKey,
        Integer quantity,
        BigDecimal refundableAmount,
        String reason,
        String detailReason,
        LocalDateTime requestedAt,
        LocalDateTime receivedAt
) {

    public static ReturnRequestSummaryResponse from(ReturnRequest returnRequest) {
        OrderItem item = returnRequest.getOrderItem();
        Claim claim = returnRequest.getClaim();
        BigDecimal refundable = item.getUnitPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity()));

        return new ReturnRequestSummaryResponse(
                returnRequest.getReturnRequestId(),
                item.getOrder().getOrderId(),
                item.getOrderItemId(),
                item.getOrder().getBuyerId(),
                item.getProductNameSnapshot(),
                item.getThumbnailKeySnapshot(),
                item.getQuantity(),
                refundable,
                claim.getReason(),
                claim.getDetailReason(),
                claim.getRequestedAt(),
                returnRequest.getReceivedAt()
        );
    }
}
