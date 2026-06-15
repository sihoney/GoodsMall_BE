package com.todaylunch.auction.infrastructure.messaging.kafka.consumer;

import com.todaylunch.auction.domain.entity.Auction;
import com.todaylunch.auction.domain.repository.AuctionRepository;
import com.todaylunch.auction.infrastructure.messaging.kafka.KafkaTopics;
import com.todaylunch.auction.infrastructure.messaging.kafka.message.ProductThumbnailChangedPayload;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductThumbnailChangedConsumer {

    private final AuctionRepository auctionRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    @KafkaListener(topics = KafkaTopics.PRODUCT_THUMBNAIL_CHANGED)
    public void handle(String payload) {
        ProductThumbnailChangedPayload message;
        try {
            message = objectMapper.readValue(payload, ProductThumbnailChangedPayload.class);
        } catch (Exception e) {
            log.error("ProductThumbnailChanged 이벤트 역직렬화 실패: payload={}", payload, e);
            return;
        }

        if (message.productId() == null || message.productId().isBlank()) {
            log.warn("ProductThumbnailChanged productId 누락: payload={}", payload);
            return;
        }

        UUID productId;
        try {
            productId = UUID.fromString(message.productId());
        } catch (IllegalArgumentException e) {
            log.warn("ProductThumbnailChanged productId 형식 오류: productId={}", message.productId());
            return;
        }

        String newThumbnailKey = message.thumbnailKey() == null ? "" : message.thumbnailKey();

        List<Auction> activeAuctions = auctionRepository.findActiveByProductId(productId);
        if (activeAuctions.isEmpty()) {
            return;
        }

        for (Auction auction : activeAuctions) {
            auction.updateThumbnailKey(newThumbnailKey);
            auctionRepository.save(auction);
        }
        log.info("경매 thumbnailKey 동기화 완료: productId={}, count={}, newKey={}",
                productId, activeAuctions.size(), newThumbnailKey);
    }
}
