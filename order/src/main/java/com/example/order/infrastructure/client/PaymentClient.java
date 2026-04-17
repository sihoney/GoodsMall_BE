package com.example.order.infrastructure.client;

import com.example.order.infrastructure.client.dto.request.ExternalPaymentRefundRequest;
import com.example.order.infrastructure.client.dto.request.ExternalPaymentRequest;
import com.example.order.infrastructure.client.dto.response.PaymentRefundResultResponse;
import com.example.order.infrastructure.client.dto.response.PaymentResultResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "payment-service", path = "/api/payments")
public interface PaymentClient {

    @PostMapping("/orders")
    PaymentResultResponse requestPayment(ExternalPaymentRequest paymentInfo);

    @PostMapping("/cancellations")
    PaymentRefundResultResponse requestRefund(ExternalPaymentRefundRequest request);
}
