package com.example.settlement.infrastructure.messaging.kafka;

import com.example.settlement.application.usecase.SettlementPayoutUseCase;
import com.example.settlement.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * payment -> settlement 정산 지급 결과 이벤트를 소비하는 Kafka consumer(소비기)다.
 */
@Slf4j
@Component
public class SellerSettlementPayoutResultEventConsumer {

    private final SettlementPayoutUseCase settlementPayoutService;
    private final ObjectMapper objectMapper;

    public SellerSettlementPayoutResultEventConsumer(SettlementPayoutUseCase settlementPayoutService, ObjectMapper objectMapper) {
        this.settlementPayoutService = settlementPayoutService;
        this.objectMapper = objectMapper;
    }

    /**
     * 지급 결과 이벤트를 settlement 상태 반영 서비스로 전달한다.
     * transport 계층에서는 비즈니스 분기 없이 이벤트를 그대로 전달하고,
     * 상태 전이 정책은 application service가 전담한다.
     */
    @KafkaListener(
            topics = "${settlement.kafka.topics.settlement-payout-result:payment.seller-payout-result}",
            groupId = "${settlement.kafka.consumer-groups.settlement-payout-result:settlement-service}",
            containerFactory = "sellerSettlementPayoutResultKafkaListenerContainerFactory"
    )
    public void listen(String eventJson) {
        try {
            SellerSettlementPayoutResultMessage event = objectMapper.readValue(eventJson, SellerSettlementPayoutResultMessage.class);
            settlementPayoutService.applyPayoutResult(event);
        } catch (Exception e) {
            log.error("Failed to process SellerSettlementPayoutResultMessage", e);
            throw new RuntimeException("Failed to deserialize SellerSettlementPayoutResultMessage", e);
        }
    }
}

