package com.example.payment.outbox.infrastructure.messaging.kafka;


import com.example.payment.common.infrastructure.messaging.kafka.KafkaTopics;
import com.example.payment.outbox.application.event.OutboxEventPendingTrigger;
import com.example.payment.wallet.application.event.SettlementCandidateCreatedEvent;
import com.example.payment.outbox.domain.entity.OutboxEvent;
import com.example.payment.outbox.domain.repository.OutboxRepository;
import com.example.payment.common.infrastructure.messaging.kafka.contract.SettlementCandidateCreatedMessage;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.time.ZoneId;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class SettlementCandidateCreatedOutboxEventSaver {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final String SETTLEMENT_CANDIDATE_CREATED_EVENT_TYPE = "SETTLEMENT_CANDIDATE_CREATED";
    private static final String PAYMENT_SOURCE = "payment-service";

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    public SettlementCandidateCreatedOutboxEventSaver(
            OutboxRepository outboxRepository,
            ObjectMapper objectMapper,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public void save(SettlementCandidateCreatedEvent event) {
        try {
            SettlementCandidateCreatedMessage payload = new SettlementCandidateCreatedMessage(
                    event.eventId(),
                    event.orderId(),
                    event.escrowId(),
                    event.sellerMemberId(),
                    event.grossAmount(),
                    event.releasedAt().atZone(KOREA_ZONE_ID).toInstant(),
                    event.confirmationType(),
                    event.occurredAt().atZone(KOREA_ZONE_ID).toInstant()
            );
            EventEnvelope<SettlementCandidateCreatedMessage> envelope = new EventEnvelope<>(
                    event.eventId(),
                    SETTLEMENT_CANDIDATE_CREATED_EVENT_TYPE,
                    PAYMENT_SOURCE,
                    event.escrowId(),
                    event.sellerMemberId(),
                    event.occurredAt().atZone(KOREA_ZONE_ID).toInstant(),
                    resolveTraceId(event),
                    payload
            );
            String message = objectMapper.writeValueAsString(envelope);
            OutboxEvent outboxEvent = OutboxEvent.create(
                    KafkaTopics.SETTLEMENT_CANDIDATE_CREATED,
                    SETTLEMENT_CANDIDATE_CREATED_EVENT_TYPE,
                    String.valueOf(event.escrowId()),
                    resolveTraceId(event),
                    message
            );
            outboxRepository.save(outboxEvent);
            applicationEventPublisher.publishEvent(new OutboxEventPendingTrigger());
        } catch (Exception e) {
            log.error("SettlementCandidateCreatedMessage 吏곷젹?붿뿉 ?ㅽ뙣?덉뒿?덈떎. escrowId={}", event.escrowId(), e);
            throw new RuntimeException("SettlementCandidateCreatedMessage 吏곷젹?붿뿉 ?ㅽ뙣?덉뒿?덈떎.", e);
        }
    }

    private String resolveTraceId(SettlementCandidateCreatedEvent event) {
        UUID eventId = event.eventId();
        if (eventId == null) {
            return "payment-settlement-candidate-created";
        }
        return eventId.toString();
    }
}
