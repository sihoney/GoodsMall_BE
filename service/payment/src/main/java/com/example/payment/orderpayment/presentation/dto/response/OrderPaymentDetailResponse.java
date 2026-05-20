package com.example.payment.orderpayment.presentation.dto.response;

import com.example.payment.orderpayment.application.dto.OrderPaymentDetailResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderPaymentDetailResponse(
        UUID orderPaymentId,
        UUID orderId,
        BigDecimal totalAmount,
        String paymentMethod,
        String paymentStatus,
        LocalDateTime paidAt
) {
    public static OrderPaymentDetailResponse from(OrderPaymentDetailResult result) {
        return new OrderPaymentDetailResponse(
                result.orderPaymentId(),
                result.orderId(),
                result.totalAmount(),
                result.paymentMethod().name(),
                result.paymentStatus().name(),
                result.paidAt()
        );
    }
}
