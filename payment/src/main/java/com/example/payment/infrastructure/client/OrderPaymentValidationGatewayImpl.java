package com.example.payment.infrastructure.client;

import com.example.payment.common.exception.InvalidCardPaymentRequestException;
import com.example.payment.domain.service.OrderPaymentValidationData;
import com.example.payment.domain.service.OrderPaymentValidationItemData;
import com.example.payment.domain.service.OrderPaymentValidationGateway;
import com.example.payment.infrastructure.client.dto.request.OrderPaymentValidationRequest;
import com.example.payment.infrastructure.client.dto.response.OrderPaymentValidationResponse;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class OrderPaymentValidationGatewayImpl implements OrderPaymentValidationGateway {

    private final OrderClient orderClient;

    public OrderPaymentValidationGatewayImpl(OrderClient orderClient) {
        this.orderClient = orderClient;
    }

    @Override
    public OrderPaymentValidationData validate(UUID orderId, UUID buyerId, java.math.BigDecimal amount) {
        try {
            OrderPaymentValidationResponse response = orderClient.validatePayment(
                    new OrderPaymentValidationRequest(orderId, buyerId, amount)
            );
            if (response == null) {
                throw new InvalidCardPaymentRequestException("order validation response is empty.");
            }

            List<OrderPaymentValidationItemData> orderItems = response.orderItems() == null
                    ? Collections.emptyList()
                    : response.orderItems().stream()
                            .map(orderItem -> new OrderPaymentValidationItemData(
                                    orderItem.orderItemId(),
                                    orderItem.sellerId(),
                                    orderItem.lineAmount()
                            ))
                            .toList();

            return new OrderPaymentValidationData(orderItems);
        } catch (RuntimeException e) {
            throw new InvalidCardPaymentRequestException("failed to validate order payment request.");
        }
    }
}
