package com.example.payment.settlement.infrastructure.messaging.kafka;


import com.example.payment.common.infrastructure.messaging.kafka.KafkaTopics;
import com.example.payment.common.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultMessage;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.time.ZoneId;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * payment -> settlement ?뺤궛 吏湲?寃곌낵 ?대깽?몃? 諛쒗뻾?섎뒗 Kafka publisher(諛쒗뻾湲???
 */
@Slf4j
@Component
public class KafkaSellerSettlementPayoutResultEventPublisher {

    private static final String SELLER_SETTLEMENT_PAYOUT_RESULT_EVENT_TYPE = "SELLER_SETTLEMENT_PAYOUT_RESULT";
    private static final String PAYMENT_SOURCE = "payment-service";
    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaSellerSettlementPayoutResultEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * settlementId ?ㅻ줈 吏湲?寃곌낵 ?대깽?몃? 諛쒗뻾?쒕떎.
     * ?붿껌 ?대깽?몄? ?숈씪???뺤궛嫄?湲곗??쇰줈 寃곌낵瑜?異붿쟻?섍린 ?쎈룄濡?settlementId瑜?key濡?怨좎젙?쒕떎.
     */
    public void publish(SellerSettlementPayoutResultMessage event) {
        try {
            EventEnvelope<SellerSettlementPayoutResultMessage> envelope = new EventEnvelope<>(
                    event.eventId(),
                    SELLER_SETTLEMENT_PAYOUT_RESULT_EVENT_TYPE,
                    PAYMENT_SOURCE,
                    event.settlementId(),
                    event.sellerMemberId(),
                    event.processedAt().atZone(KOREA_ZONE_ID).toInstant(),
                    resolveTraceId(event),
                    event
            );
            String message = objectMapper.writeValueAsString(envelope);
            kafkaTemplate.send(KafkaTopics.SETTLEMENT_PAYOUT_RESULT, String.valueOf(event.settlementId()), message);
        } catch (Exception e) {
            log.error("SellerSettlementPayoutResultMessage 吏곷젹?붿뿉 ?ㅽ뙣?덉뒿?덈떎. settlementId={}", event.settlementId(), e);
            throw new RuntimeException("SellerSettlementPayoutResultMessage 吏곷젹?붿뿉 ?ㅽ뙣?덉뒿?덈떎.", e);
        }
    }

    private String resolveTraceId(SellerSettlementPayoutResultMessage event) {
        UUID requestEventId = event.requestEventId();
        if (requestEventId != null) {
            return requestEventId.toString();
        }
        UUID eventId = event.eventId();
        if (eventId != null) {
            return eventId.toString();
        }
        return "payment-seller-settlement-payout-result";
    }
}

