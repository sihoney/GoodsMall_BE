package com.example.payment.infrastructure.client;

import com.example.payment.common.exception.InvalidCardPaymentRequestException;
import com.example.payment.domain.service.OrderPaymentValidationData;
import com.example.payment.domain.service.OrderPaymentValidationItemData;
import com.example.payment.domain.service.OrderPaymentValidationGateway;
import com.example.payment.infrastructure.client.dto.request.OrderPaymentValidationRequest;
import com.example.payment.infrastructure.client.dto.response.OrderPaymentValidationResponse;
import feign.FeignException;
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
            var response = orderClient.validatePayment(
                    orderId,
                    new OrderPaymentValidationRequest(buyerId, amount)
            );
            if (response == null) {
                throw new InvalidCardPaymentRequestException("주문 서비스 결제 검증 응답이 비어 있습니다.");
            }
            if (!response.success()) {
                String message = response.error() != null && response.error().message() != null
                        ? response.error().message()
                        : "주문 서비스 결제 검증에 실패했습니다.";
                throw new InvalidCardPaymentRequestException("주문 서비스 결제 검증에 실패했습니다. " + message);
            }
            if (response.data() == null) {
                throw new InvalidCardPaymentRequestException("주문 서비스 결제 검증 응답 데이터가 비어 있습니다.");
            }

            List<OrderPaymentValidationItemData> orderItems = response.data().items() == null
                    ? Collections.emptyList()
                    : response.data().items().stream()
                            .map(orderItem -> new OrderPaymentValidationItemData(
                                    orderItem.orderItemId(),
                                    orderItem.sellerId(),
                                    orderItem.lineAmount()
                            ))
                            .toList();

            return new OrderPaymentValidationData(orderItems);
        } catch (FeignException e) {
            throw new InvalidCardPaymentRequestException(
                    "주문 서비스 결제 검증 호출에 실패했습니다. status=%s body=%s"
                            .formatted(e.status(), e.contentUTF8()),
                    e
            );
        } catch (RuntimeException e) {
            if (e instanceof InvalidCardPaymentRequestException) {
                throw e;
            }
            throw new InvalidCardPaymentRequestException("주문 결제 검증 중 오류가 발생했습니다.", e);
        }
    }
}
