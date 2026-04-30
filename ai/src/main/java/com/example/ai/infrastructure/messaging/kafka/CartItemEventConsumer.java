package com.example.ai.infrastructure.messaging.kafka;

import com.example.ai.application.usecase.UserRecommendationUseCase;
import com.example.ai.infrastructure.messaging.kafka.contract.CartItemAddedMessage;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartItemEventConsumer {

    private final UserRecommendationUseCase userRecommendationUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopics.CART_ITEM_ADDED,
            groupId = KafkaConsumerGroups.AI_CART_RECOMMENDATION_GROUP,
            containerFactory = "cartEventKafkaListenerContainerFactory"
    )
    public void consumeCartItemAdded(String payload) {
        CartItemAddedMessage message = parse(payload);
        if (message == null) {
            return;
        }

        UUID memberId = message.memberId();
        UUID productId = message.productId();

        if (memberId == null || productId == null) {
            log.warn("장바구니 이벤트 필드 누락 payload={}", payload);
            return;
        }

        try {
            userRecommendationUseCase.updateRecommendationsForUser(memberId, productId);
        } catch (RuntimeException e) {
            log.warn("사용자 추천 업데이트 실패 memberId={} productId={}", memberId, productId, e);
        }
    }

    private CartItemAddedMessage parse(String payload) {
        try {
            return objectMapper.readValue(payload, CartItemAddedMessage.class);
        } catch (JacksonException e) {
            log.warn("장바구니 이벤트 역직렬화 실패 payload={}", payload, e);
            return null;
        }
    }
}
