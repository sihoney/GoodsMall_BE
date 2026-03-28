package com.example.settlement.infrastructure.messaging.kafka;

import com.example.settlement.application.service.SettlementPayoutService;
import com.example.settlement.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * payment -> settlement 정산 지급 결과 이벤트를 소비하는 Kafka consumer(소비기)다.
 */
@Component
public class SellerSettlementPayoutResultEventConsumer {

    private final SettlementPayoutService settlementPayoutService;

    public SellerSettlementPayoutResultEventConsumer(SettlementPayoutService settlementPayoutService) {
        this.settlementPayoutService = settlementPayoutService;
    }

    /**
     * 지급 결과 이벤트를 settlement 상태 반영 서비스로 전달한다.
     */
    @KafkaListener(
            topics = "${settlement.kafka.topics.settlement-payout-result:payment.seller-payout-result}",
            groupId = "${settlement.kafka.consumer-groups.settlement-payout-result:settlement-service}",
            containerFactory = "sellerSettlementPayoutResultKafkaListenerContainerFactory"
    )
    public void listen(SellerSettlementPayoutResultMessage event) {
        settlementPayoutService.applyPayoutResult(event);
    }
}

