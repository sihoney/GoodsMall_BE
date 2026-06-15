package com.example.order.presentation.dto.response;

import com.example.order.domain.entity.Claim;
import com.example.order.domain.entity.OrderItem;
import com.example.order.domain.entity.ReturnRequest;
import com.example.order.domain.enumtype.ResponsibilityType;
import com.example.order.domain.enumtype.ReturnRequestStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReturnRequestSummaryResponse(
        UUID returnRequestId,
        UUID orderId,
        UUID orderItemId,
        UUID buyerId,
        String receiver,
        String productName,
        String thumbnailKey,
        Integer quantity,
        BigDecimal refundableAmount,
        String reason,
        String detailReason,
        LocalDateTime requestedAt,
        LocalDateTime receivedAt,
        ReturnRequestStatus status,
        LocalDateTime processedAt,
        ResponsibilityType responsibilityType,
        BigDecimal refundedAmount,
        String rejectReason
) {

    public static ReturnRequestSummaryResponse from(ReturnRequest returnRequest) {
        OrderItem item = returnRequest.getOrderItem();
        Claim claim = returnRequest.getClaim();
        BigDecimal refundable = item.getUnitPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity()));

        boolean isCompleted = returnRequest.getStatus() == ReturnRequestStatus.COMPLETED;
        boolean isFailed = returnRequest.getStatus() == ReturnRequestStatus.FAILED;

        return new ReturnRequestSummaryResponse(
                returnRequest.getReturnRequestId(),
                item.getOrder().getOrderId(),
                item.getOrderItemId(),
                item.getOrder().getBuyerId(),
                item.getOrder().getReceiver(),
                item.getProductNameSnapshot(),
                item.getThumbnailKeySnapshot(),
                item.getQuantity(),
                refundable,
                claim.getReason(),
                claim.getDetailReason(),
                claim.getRequestedAt(),
                returnRequest.getReceivedAt(),
                returnRequest.getStatus(),
                isCompleted || isFailed ? returnRequest.getUpdatedAt() : null,
                isCompleted ? claim.getResponsibilityType() : null,
                isCompleted ? refundable : null,
                isFailed ? returnRequest.getFailReason() : null
        );
    }
}
