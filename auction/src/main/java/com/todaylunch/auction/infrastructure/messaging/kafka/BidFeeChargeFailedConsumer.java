package com.todaylunch.auction.infrastructure.messaging.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaylunch.auction.common.exception.application.BidNotFoundException;
import com.todaylunch.auction.domain.entity.Bid;
import com.todaylunch.auction.domain.enumtype.BidStatus;
import com.todaylunch.auction.domain.repository.BidRepository;
import com.todaylunch.auction.infrastructure.messaging.kafka.message.BidFeeChargeFailedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * payment에서 수수료 차감이 실패했을 때 발행하는 이벤트를 소비한다.
 * Bid PENDING → CANCELED 전이.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BidFeeChargeFailedConsumer {

    private final BidRepository bidRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${auction.kafka.topic.bid-fee-charge-failed:auction.bid-fee.charge-failed}")
    @Transactional
    public void handle(String payload) throws Exception {
        BidFeeChargeFailedMessage message = objectMapper.readValue(payload, BidFeeChargeFailedMessage.class);

        Bid bid = bidRepository.findById(message.bidId())
                .orElseThrow(BidNotFoundException::new);

        if (bid.getStatus() != BidStatus.PENDING) {
            log.warn("중복 이벤트 또는 잘못된 상태 — 무시: bidId={}, status={}",
                    bid.getBidId(), bid.getStatus());
            return;
        }

        bid.cancel();

        log.warn("Bid canceled via kafka: bidId={}, reason={}, message={}",
                bid.getBidId(), message.failureReason(), message.failureMessage());

        // TODO: 사용자 개인 채널 WebSocket 메시지 전송 (개인 채널 구현 시 추가)
    }
}
