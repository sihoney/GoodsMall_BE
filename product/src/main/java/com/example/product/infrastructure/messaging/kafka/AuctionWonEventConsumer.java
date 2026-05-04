package com.example.product.infrastructure.messaging.kafka;

import com.example.product.common.exception.ProductNotFoundException;
import com.example.product.domain.entity.Product;
import com.example.product.domain.repository.ProductRepository;
import com.example.product.infrastructure.messaging.kafka.contract.AuctionWonPayload;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.todaylunch.common.event.contract.EventEnvelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionWonEventConsumer {

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    @KafkaListener(topics = KafkaTopics.AUCTION_WON)
    public void handle(String message) {
        try {
            EventEnvelope<AuctionWonPayload> envelope = objectMapper.readValue(
                    message, new TypeReference<>() {}
            );
            AuctionWonPayload payload = envelope.payload();

            Product product = productRepository.findByIdWithLock(payload.productId())
                    .orElseThrow(ProductNotFoundException::new);

            product.decreaseStock(1);
            product.updateAuctionFinalPrice(payload.finalPrice());
            productRepository.save(product);
            log.debug("경매 낙찰 상품 처리 완료: productId={}, finalPrice={}", payload.productId(), payload.finalPrice());
        } catch (ProductNotFoundException e) {
            log.error("경매 낙찰 상품 없음: message={}", message, e);
        } catch (Exception e) {
            log.error("경매 낙찰 이벤트 처리 실패: message={}", message, e);
        }
    }
}
