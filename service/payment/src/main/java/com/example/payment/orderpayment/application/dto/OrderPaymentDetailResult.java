package com.example.payment.orderpayment.application.dto;

import com.example.payment.orderpayment.domain.enumtype.OrderPaymentMethod;
import com.example.payment.orderpayment.domain.enumtype.OrderPaymentStatus;

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
