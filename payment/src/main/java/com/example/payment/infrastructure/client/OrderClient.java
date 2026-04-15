package com.example.payment.infrastructure.client;

import com.example.payment.infrastructure.client.dto.request.OrderRefundCompletedRequest;
import com.example.payment.infrastructure.client.dto.request.OrderPaymentValidationRequest;
import com.example.payment.infrastructure.client.dto.response.OrderRefundCompletedResponse;
import com.example.payment.infrastructure.client.dto.response.OrderPaymentValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "order-service",
        url = "${services.order.url:http://localhost:8081}"
)
public interface OrderClient {

    @PostMapping("/api/orders/payment-validation")
    OrderPaymentValidationResponse validatePayment(@RequestBody OrderPaymentValidationRequest request);

    @PostMapping("/api/orders/refunds/completed")
    OrderRefundCompletedResponse notifyRefundCompleted(@RequestBody OrderRefundCompletedRequest request);
}
