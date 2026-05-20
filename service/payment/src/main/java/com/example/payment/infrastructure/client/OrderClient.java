package com.example.payment.infrastructure.client;

import com.example.payment.infrastructure.client.dto.request.OrderRefundCompletedRequest;
import com.example.payment.infrastructure.client.dto.request.OrderPaymentValidationRequest;
import com.example.payment.infrastructure.client.dto.response.OrderApiResponse;
import com.example.payment.infrastructure.client.dto.response.OrderRefundCompletedResponse;
import com.example.payment.infrastructure.client.dto.response.OrderPaymentValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "order-service",
        url = "${services.order.url:http://localhost:8084}"
)
public interface OrderClient {

    @PostMapping("/api/orders/{orderId}/payment-validation")
    OrderApiResponse<OrderPaymentValidationResponse> validatePayment(
            @PathVariable("orderId") java.util.UUID orderId,
            @RequestBody OrderPaymentValidationRequest request
    );

    @PostMapping("/api/orders/refunds/completed")
    OrderRefundCompletedResponse notifyRefundCompleted(@RequestBody OrderRefundCompletedRequest request);
}
