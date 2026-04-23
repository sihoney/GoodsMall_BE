package com.example.settlement.infrastructure.messaging.kafka;

import com.example.settlement.application.dto.SettlementItemCreateCommand;
import com.example.settlement.application.usecase.MonthlySettlementUseCase;
import com.example.settlement.infrastructure.messaging.kafka.contract.SettlementCandidateCreatedMessage;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * payment -> settlement 정산 후보 알림 이벤트를 소비해 settlement item으로 적재한다.
 */
@Slf4j
@Component
public class SettlementCandidateCreatedEventConsumer {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final MonthlySettlementUseCase monthlySettlementService;
    private final ObjectMapper objectMapper;

    public SettlementCandidateCreatedEventConsumer(MonthlySettlementUseCase monthlySettlementService, ObjectMapper objectMapper) {
        this.monthlySettlementService = monthlySettlementService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = KafkaTopics.SETTLEMENT_CANDIDATE_CREATED,
            groupId = KafkaConsumerGroups.SETTLEMENT_SERVICE,
            containerFactory = "settlementCandidateCreatedKafkaListenerContainerFactory"
    )
    public void listen(String eventJson) {
        try {
            // objectMapper를 활용해 JSON 문자열을 SettlementCandidateCreatedMessage 객체로 변환한다.
            SettlementCandidateCreatedMessage event = objectMapper.readValue(eventJson, SettlementCandidateCreatedMessage.class);
            validateEvent(event);
            monthlySettlementService.registerSettlementItem(new SettlementItemCreateCommand(
                    event.orderId(),
                    event.escrowId(),
                    event.sellerMemberId(),
                    event.grossAmount(),
                    toKoreaLocalDateTime(event.releasedAt())
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
        if (event.grossAmount() == null || event.grossAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("grossAmount must be positive.");
        }
        if (event.releasedAt() == null) {
            throw new IllegalArgumentException("releasedAt is required.");
        }
    }

    private LocalDateTime toKoreaLocalDateTime(java.time.Instant instant) {
        return LocalDateTime.ofInstant(instant, KOREA_ZONE_ID);
    }
}
