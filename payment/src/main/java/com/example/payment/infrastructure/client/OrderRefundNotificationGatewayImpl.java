package com.example.payment.infrastructure.client;

import com.example.payment.domain.service.OrderRefundNotificationGateway;
import com.example.payment.infrastructure.client.dto.request.OrderRefundCompletedRequest;
import com.example.payment.infrastructure.client.dto.response.OrderRefundCompletedResponse;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderRefundNotificationGatewayImpl implements OrderRefundNotificationGateway {

    private final OrderClient orderClient;

    public OrderRefundNotificationGatewayImpl(OrderClient orderClient) {
        this.orderClient = orderClient;
    }

    @Override
    public boolean notifyRefundCompleted(UUID orderId, List<UUID> orderItemIds) {
        try {
            OrderRefundCompletedResponse response = orderClient.notifyRefundCompleted(
                    new OrderRefundCompletedRequest(orderId, orderItemIds)
            );
            return response != null && response.success();
        } catch (RuntimeException exception) {
            log.warn("주문 환불 완료 알림 전송에 실패했습니다. orderId={}", orderId, exception);
            return false;
        }
    }
}
