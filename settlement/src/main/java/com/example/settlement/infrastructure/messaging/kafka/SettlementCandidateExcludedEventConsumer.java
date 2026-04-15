package com.example.settlement.infrastructure.messaging.kafka;

import com.example.settlement.application.dto.SettlementRefundExclusionCommand;
import com.example.settlement.application.usecase.MonthlySettlementUseCase;
import com.example.settlement.infrastructure.messaging.kafka.contract.SettlementCandidateExcludedMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SettlementCandidateExcludedEventConsumer {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final MonthlySettlementUseCase monthlySettlementService;
    private final ObjectMapper objectMapper;

    public SettlementCandidateExcludedEventConsumer(
            MonthlySettlementUseCase monthlySettlementService,
            ObjectMapper objectMapper
    ) {
        this.monthlySettlementService = monthlySettlementService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${settlement.kafka.topics.settlement-candidate-excluded:payment.settlement-candidate-excluded}",
            groupId = "${settlement.kafka.consumer-groups.settlement-candidate-excluded:settlement-service}",
            containerFactory = "settlementCandidateCreatedKafkaListenerContainerFactory"
    )
    public void listen(String eventJson) {
        try {
            SettlementCandidateExcludedMessage event =
                    objectMapper.readValue(eventJson, SettlementCandidateExcludedMessage.class);
            validateEvent(event);
            monthlySettlementService.applyRefundExclusion(new SettlementRefundExclusionCommand(
                    event.eventId(),
                    event.refundId(),
                    event.orderId(),
                    event.escrowId(),
                    event.orderItemId(),
                    event.sellerMemberId(),
                    event.buyerMemberId(),
                    event.refundAmount(),
                    toKoreaLocalDateTime(event.occurredAt())
            ));
        } catch (Exception e) {
            log.error("Failed to process SettlementCandidateExcludedMessage", e);
            throw new RuntimeException("Failed to deserialize SettlementCandidateExcludedMessage", e);
        }
    }

    private void validateEvent(SettlementCandidateExcludedMessage event) {
        if (event == null) {
            throw new IllegalArgumentException("settlementCandidateExcluded event is required.");
        }
        if (event.eventId() == null) {
            throw new IllegalArgumentException("eventId is required.");
        }
        if (event.refundId() == null) {
            throw new IllegalArgumentException("refundId is required.");
        }
        if (event.orderId() == null) {
            throw new IllegalArgumentException("orderId is required.");
        }
        if (event.escrowId() == null) {
            throw new IllegalArgumentException("escrowId is required.");
        }
        if (event.orderItemId() == null) {
            throw new IllegalArgumentException("orderItemId is required.");
        }
        if (event.sellerMemberId() == null) {
            throw new IllegalArgumentException("sellerMemberId is required.");
        }
        if (event.buyerMemberId() == null) {
            throw new IllegalArgumentException("buyerMemberId is required.");
        }
        if (event.refundAmount() == null || event.refundAmount() <= 0L) {
            throw new IllegalArgumentException("refundAmount must be positive.");
        }
        if (event.occurredAt() == null) {
            throw new IllegalArgumentException("occurredAt is required.");
        }
    }

    private LocalDateTime toKoreaLocalDateTime(java.time.Instant instant) {
        return LocalDateTime.ofInstant(instant, KOREA_ZONE_ID);
    }
}
