package com.example.order.infrastructure.redis;

import com.example.order.application.service.DeliveryShipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryExpireListener implements MessageListener {

    private static final String DELIVERY_EXPIRE_PREFIX = "delivery:expire:";

    private final DeliveryShipService deliveryShipService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        // keyspace 이벤트: channel = __keyspace@{db}__:{key}, body = 이벤트명("expired")
        String event = new String(message.getBody());
        if (!"expired".equals(event)) {
            return;
        }

        String channel = new String(message.getChannel());
        String key = channel.substring(channel.indexOf("__:") + 3);

        String deliveryIdStr = key.substring(DELIVERY_EXPIRE_PREFIX.length());
        UUID deliveryId = UUID.fromString(deliveryIdStr);

        log.info("배송 만료 이벤트 수신 - deliveryId: {}", deliveryId);
        deliveryShipService.startShip(deliveryId);
    }
}