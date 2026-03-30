package com.example.settlement.infrastructure.messaging.kafka;

import com.example.settlement.application.dto.SettlementItemCreateCommand;
import com.example.settlement.application.usecase.MonthlySettlementUseCase;
import com.example.settlement.infrastructure.messaging.kafka.contract.SettlementCandidateCreatedMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * payment -> settlement 정산 원천 후보 이벤트를 소비해 settlement item을 적재한다.
 */
@Slf4j
@Component
public class SettlementCandidateCreatedEventConsumer {

    private final MonthlySettlementUseCase monthlySettlementService;
    private final ObjectMapper objectMapper;

    public SettlementCandidateCreatedEventConsumer(MonthlySettlementUseCase monthlySettlementService, ObjectMapper objectMapper) {
        this.monthlySettlementService = monthlySettlementService;
        this.objectMapper = objectMapper;
    }

    /**
     * 지급 완료 이벤트의 정산 후보 데이터를 월 정산 원천 커맨드로 변환해 서비스에 위임한다.
     */
    @KafkaListener(
            topics = "${settlement.kafka.topics.settlement-candidate-created:payment.settlement-candidate-created}",
            groupId = "${settlement.kafka.consumer-groups.settlement-candidate-created:settlement-service}",
            containerFactory = "settlementCandidateCreatedKafkaListenerContainerFactory"
    )
    public void listen(String eventJson) {
        try {
            SettlementCandidateCreatedMessage event = objectMapper.readValue(eventJson, SettlementCandidateCreatedMessage.class);
            validateEvent(event);
            monthlySettlementService.registerSettlementItem(new SettlementItemCreateCommand(
                    event.orderId(),
                    event.escrowId(),
                    event.sellerMemberId(),
                    event.grossAmount(),
                    event.releasedAt()
            ));
        } catch (Exception e) {
            log.error("Failed to process SettlementCandidateCreatedMessage", e);
            throw new RuntimeException("Failed to deserialize SettlementCandidateCreatedMessage", e);
        }
    }

    private void validateEvent(SettlementCandidateCreatedMessage event) {
        if (event == null) {
            throw new IllegalArgumentException("settlementCandidateCreated event is required.");
        }
        if (event.orderId() == null) {
            throw new IllegalArgumentException("orderId is required.");
        }
        if (event.escrowId() == null) {
            throw new IllegalArgumentException("escrowId is required.");
        }
        if (event.sellerMemberId() == null) {
            throw new IllegalArgumentException("sellerMemberId is required.");
        }
        if (event.grossAmount() == null || event.grossAmount() <= 0) {
            throw new IllegalArgumentException("grossAmount must be positive.");
        }
        if (event.releasedAt() == null) {
            throw new IllegalArgumentException("releasedAt is required.");
        }
    }
}
