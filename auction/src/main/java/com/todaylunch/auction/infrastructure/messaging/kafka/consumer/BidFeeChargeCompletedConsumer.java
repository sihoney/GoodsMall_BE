package com.todaylunch.auction.infrastructure.messaging.kafka.consumer;

import tools.jackson.databind.ObjectMapper;
import com.todaylunch.auction.application.event.BidPlacedEvent;
import com.todaylunch.auction.common.exception.application.BidNotFoundException;
import com.todaylunch.auction.domain.entity.Bid;
import com.todaylunch.auction.domain.enumtype.BidStatus;
import com.todaylunch.auction.domain.repository.BidRepository;
import com.todaylunch.auction.infrastructure.messaging.kafka.KafkaTopics;
import com.todaylunch.auction.infrastructure.messaging.kafka.message.BidFeeChargeCompletedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * payment에서 수수료 차감이 성공했을 때 발행하는 이벤트를 소비한다.
 * Bid PENDING → ACTIVE 전이, 이전 ACTIVE → OUTBID, WebSocket 브로드캐스트.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BidFeeChargeCompletedConsumer {

    private final BidRepository bidRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.BID_FEE_CHARGE_COMPLETED)
    @Transactional
    public void handle(String payload) throws Exception {
        BidFeeChargeCompletedMessage message
                = objectMapper.readValue(payload, BidFeeChargeCompletedMessage.class);

        Bid bid = bidRepository.findById(message.bidId())
                .orElseThrow(BidNotFoundException::new);

        if (bid.getStatus() != BidStatus.PENDING) {
            log.warn("중복 이벤트 또는 잘못된 상태 — 무시: bidId={}, status={}",
                    bid.getBidId(), bid.getStatus());
            return;
        }

        bid.confirm();

        bidRepository.findActiveByAuctionId(bid.getAuction().getAuctionId())
                .filter(active -> !active.getBidId().equals(bid.getBidId()))
                .ifPresent(Bid::outbid);

        applicationEventPublisher.publishEvent(new BidPlacedEvent(
                bid.getAuction().getAuctionId(),
                bid.getBidderId(),
                bid.getBidPrice(),
                bid.getAuction().getEndedAt()
        ));

        log.info("Bid confirmed via kafka: bidId={}, auctionId={}", bid.getBidId(), message.auctionId());
    }
}
