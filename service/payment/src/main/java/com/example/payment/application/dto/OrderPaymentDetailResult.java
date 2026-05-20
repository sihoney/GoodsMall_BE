package com.example.payment.application.dto;

import com.example.payment.domain.enumtype.OrderPaymentMethod;
import com.example.payment.domain.enumtype.OrderPaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderPaymentDetailResult(
        UUID orderPaymentId,
        UUID orderId,
        BigDecimal totalAmount,
        OrderPaymentMethod paymentMethod,
        OrderPaymentStatus paymentStatus,
        LocalDateTime paidAt
) {
}
