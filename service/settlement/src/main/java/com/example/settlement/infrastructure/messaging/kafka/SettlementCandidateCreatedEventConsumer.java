package com.example.settlement.infrastructure.messaging.kafka;

import com.example.settlement.application.dto.SettlementItemCreateCommand;
import com.example.settlement.application.usecase.MonthlySettlementUseCase;
import com.example.settlement.infrastructure.messaging.kafka.exception.SettlementKafkaProcessingException;
import com.example.settlement.infrastructure.messaging.kafka.exception.SettlementKafkaValidationException;
import com.example.settlement.infrastructure.messaging.kafka.contract.SettlementCandidateCreatedMessage;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * payment -> settlement 정산 후보 알림 이벤트를 소비해 settlement item으로 적재한다.
 */
@Slf4j
@Component
public class SettlementCandidateCreatedEventConsumer {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final String SETTLEMENT_CANDIDATE_CREATED_EVENT_TYPE = "SETTLEMENT_CANDIDATE_CREATED";
    private static final TypeReference<EventEnvelope<SettlementCandidateCreatedMessage>>
            SETTLEMENT_CANDIDATE_CREATED_ENVELOPE_TYPE =
            new TypeReference<>() {
            };

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
            EventEnvelope<SettlementCandidateCreatedMessage> envelope = readEnvelope(eventJson);
            validateEnvelope(envelope);
            SettlementCandidateCreatedMessage event = envelope.payload();
            monthlySettlementService.registerSettlementItem(new SettlementItemCreateCommand(
                    event.orderId(),
                    event.escrowId(),
                    event.sellerMemberId(),
                    event.grossAmount(),
                    toKoreaLocalDateTime(event.releasedAt())
            ));
        } catch (SettlementKafkaValidationException exception) {
            log.warn("정산 후보 생성 Kafka 메시지를 DLQ 대상으로 분류합니다. message={}", exception.getMessage(), exception);
            throw exception;
        } catch (IllegalArgumentException exception) {
            log.warn("정산 후보 생성 payload 검증에 실패했습니다. message={}", exception.getMessage(), exception);
            throw new SettlementKafkaValidationException(exception.getMessage(), exception);
        } catch (Exception exception) {
            log.error("정산 후보 생성 Kafka 메시지 처리 중 재시도 대상 오류가 발생했습니다.", exception);
            throw new SettlementKafkaProcessingException("정산 후보 생성 Kafka 메시지 처리에 실패했습니다.", exception);
        }
    }

    private EventEnvelope<SettlementCandidateCreatedMessage> readEnvelope(String eventJson) {
        try {
            return objectMapper.readValue(eventJson, SETTLEMENT_CANDIDATE_CREATED_ENVELOPE_TYPE);
        } catch (Exception exception) {
            throw new SettlementKafkaValidationException("정산 후보 생성 envelope 역직렬화에 실패했습니다.", exception);
        }
    }

    private void validateEnvelope(EventEnvelope<SettlementCandidateCreatedMessage> envelope) {
        if (envelope == null) {
            throw new SettlementKafkaValidationException("settlementCandidateCreated envelope는 필수입니다.");
        }
        if (envelope.eventId() == null) {
            throw new SettlementKafkaValidationException("eventId는 필수입니다.");
        }
        if (envelope.eventType() == null || envelope.eventType().isBlank()) {
            throw new SettlementKafkaValidationException("eventType은 필수입니다.");
        }
        if (!SETTLEMENT_CANDIDATE_CREATED_EVENT_TYPE.equals(envelope.eventType())) {
            throw new SettlementKafkaValidationException("eventType이 올바르지 않습니다.");
        }
        if (envelope.source() == null || envelope.source().isBlank()) {
            throw new SettlementKafkaValidationException("source는 필수입니다.");
        }
        if (envelope.aggregateId() == null) {
            throw new SettlementKafkaValidationException("aggregateId는 필수입니다.");
        }
        if (envelope.occurredAt() == null) {
            throw new SettlementKafkaValidationException("occurredAt은 필수입니다.");
        }
        if (envelope.traceId() == null || envelope.traceId().isBlank()) {
            throw new SettlementKafkaValidationException("traceId는 필수입니다.");
        }

        SettlementCandidateCreatedMessage event = envelope.payload();
        if (event == null) {
            throw new SettlementKafkaValidationException("payload는 필수입니다.");
        }
        if (event.orderId() == null) {
            throw new SettlementKafkaValidationException("orderId는 필수입니다.");
        }
        if (event.escrowId() == null) {
            throw new SettlementKafkaValidationException("escrowId는 필수입니다.");
        }
        if (event.sellerMemberId() == null) {
            throw new SettlementKafkaValidationException("sellerMemberId는 필수입니다.");
        }
        if (event.grossAmount() == null || event.grossAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new SettlementKafkaValidationException("grossAmount는 0보다 커야 합니다.");
        }
        if (event.releasedAt() == null) {
            throw new SettlementKafkaValidationException("releasedAt은 필수입니다.");
        }
        if (!envelope.aggregateId().equals(event.escrowId())) {
            throw new SettlementKafkaValidationException("aggregateId는 escrowId와 같아야 합니다.");
        }
        if (envelope.recipientId() != null && !envelope.recipientId().equals(event.sellerMemberId())) {
            throw new SettlementKafkaValidationException("recipientId는 sellerMemberId와 같아야 합니다.");
        }
    }

    private LocalDateTime toKoreaLocalDateTime(java.time.Instant instant) {
        return LocalDateTime.ofInstant(instant, KOREA_ZONE_ID);
    }
}
