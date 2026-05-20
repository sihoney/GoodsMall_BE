package com.example.payment.refund.infrastructure.client;


import com.example.payment.common.infrastructure.client.OrderClient;
import com.example.payment.refund.domain.service.OrderRefundNotificationGateway;
import com.example.payment.refund.infrastructure.client.dto.request.OrderRefundCompletedRequest;
import com.example.payment.refund.infrastructure.client.dto.response.OrderRefundCompletedResponse;
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
            log.warn("二쇰Ц ?섎텋 ?꾨즺 ?뚮┝ ?꾩넚???ㅽ뙣?덉뒿?덈떎. orderId={}", orderId, exception);
            return false;
        }
    }
}
