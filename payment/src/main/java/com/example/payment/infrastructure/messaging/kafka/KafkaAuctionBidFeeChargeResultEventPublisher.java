package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.infrastructure.messaging.kafka.contract.BidFeeChargeFailedMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.BidFeeChargeSucceededMessage;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * payment -> auction 경매 입찰 보증금 처리 결과 이벤트를 발행하는 Kafka publisher다.
 */
@Slf4j
@Component
public class KafkaAuctionBidFeeChargeResultEventPublisher {

    private static final String BID_FEE_CHARGE_SUCCEEDED_EVENT_TYPE = "BID_FEE_CHARGE_SUCCEEDED";
    private static final String BID_FEE_CHARGE_FAILED_EVENT_TYPE = "BID_FEE_CHARGE_FAILED";
    private static final String PAYMENT_SOURCE = "payment-service";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaAuctionBidFeeChargeResultEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishSuccess(BidFeeChargeSucceededMessage event) {
        publish(KafkaTopics.AUCTION_BID_FEE_CHARGE_SUCCEEDED, String.valueOf(event.auctionId()), event);
    }

    public void publishFailure(BidFeeChargeFailedMessage event) {
        publish(KafkaTopics.AUCTION_BID_FEE_CHARGE_FAILED, String.valueOf(event.auctionId()), event);
    }

    private void publish(String topic, String key, Object event) {
        try {
            String message = objectMapper.writeValueAsString(buildEnvelope(event));
            kafkaTemplate.send(topic, key, message);
        } catch (Exception e) {
            log.error("경매 입찰 보증금 처리 결과 이벤트 직렬화에 실패했습니다. topic={} key={}", topic, key, e);
            throw new RuntimeException("경매 입찰 보증금 처리 결과 이벤트 직렬화에 실패했습니다.", e);
        }
    }

    private EventEnvelope<?> buildEnvelope(Object event) {
        if (event instanceof BidFeeChargeSucceededMessage successEvent) {
            return new EventEnvelope<>(
                    successEvent.eventId(),
                    BID_FEE_CHARGE_SUCCEEDED_EVENT_TYPE,
                    PAYMENT_SOURCE,
                    successEvent.auctionId(),
                    null,
                    successEvent.occurredAt(),
                    resolveTraceId(successEvent.eventId(), successEvent.bidId(), "payment-bid-fee-charge-succeeded"),
                    successEvent
            );
        }
        if (event instanceof BidFeeChargeFailedMessage failedEvent) {
            return new EventEnvelope<>(
                    failedEvent.eventId(),
                    BID_FEE_CHARGE_FAILED_EVENT_TYPE,
                    PAYMENT_SOURCE,
                    failedEvent.auctionId(),
                    null,
                    failedEvent.occurredAt(),
                    resolveTraceId(failedEvent.eventId(), failedEvent.bidId(), "payment-bid-fee-charge-failed"),
                    failedEvent
            );
        }
        throw new IllegalArgumentException("지원하지 않는 입찰 보증금 결과 이벤트입니다.");
    }

    private String resolveTraceId(UUID eventId, UUID bidId, String fallback) {
        if (bidId != null) {
            return bidId.toString();
        }
        if (eventId != null) {
            return eventId.toString();
        }
        return fallback;
    }
}
