package com.example.order.infrastructure.client;

import com.example.order.infrastructure.client.dto.request.ExternalPaymentRequest;
import com.example.order.infrastructure.client.dto.response.PaymentResultResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "payment-service", url = "http://localhost:8082")
public interface PaymentClient {

    @PostMapping("/api/payments/orders")
    PaymentResultResponse requestPayment(ExternalPaymentRequest paymentInfo);
}
