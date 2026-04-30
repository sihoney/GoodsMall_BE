package com.example.cartservice.cart.infrastructure.kafka;

import com.example.cartservice.cart.infrastructure.kafka.event.CartItemAddedEvent;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartEventPublisher {

    static final String CART_ITEM_ADDED_TOPIC = "cart.item.added";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishCartItemAdded(UUID memberId, UUID productId) {
        try {
            CartItemAddedEvent event = new CartItemAddedEvent(memberId, productId);
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(CART_ITEM_ADDED_TOPIC, memberId.toString(), payload);
        } catch (JacksonException e) {
            log.warn("장바구니 이벤트 발행 실패 memberId={}, productId={}", memberId, productId, e);
        }
    }
}
