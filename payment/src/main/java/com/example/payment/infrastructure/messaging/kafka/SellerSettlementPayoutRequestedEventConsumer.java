package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.common.exception.WalletNotFoundException;
import com.example.payment.domain.entity.Wallet;
import com.example.payment.domain.entity.WalletTransaction;
import com.example.payment.domain.enumtype.WalletTransactionType;
import com.example.payment.domain.repository.WalletRepository;
import com.example.payment.domain.repository.WalletTransactionRepository;
import com.example.payment.domain.service.IdentifierGenerator;
import com.example.payment.domain.service.TimeProvider;
import com.example.payment.infrastructure.messaging.kafka.contract.PayoutFailureReason;
import com.example.payment.infrastructure.messaging.kafka.contract.SellerSettlementPayoutRequestedMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultStatus;
import com.example.payment.infrastructure.messaging.kafka.contract.SettlementPayoutType;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.time.LocalDateTime;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * settlement -> payment 정산 지급 요청 이벤트를 소비하고 wallet 적립을 처리하는 Kafka consumer(소비기)다.
 */
@Component
@Transactional
public class SellerSettlementPayoutRequestedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(SellerSettlementPayoutRequestedEventConsumer.class);
    private static final String SELLER_SETTLEMENT_PAYOUT_REQUESTED_EVENT_TYPE = "SELLER_SETTLEMENT_PAYOUT_REQUESTED";
    private static final TypeReference<EventEnvelope<SellerSettlementPayoutRequestedMessage>>
            SELLER_SETTLEMENT_PAYOUT_REQUESTED_ENVELOPE_TYPE = new TypeReference<>() {
    };

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final IdentifierGenerator identifierGenerator;
    private final TimeProvider timeProvider;
    private final KafkaSellerSettlementPayoutResultEventPublisher payoutResultEventPublisher;
    private final ObjectMapper objectMapper;

    public SellerSettlementPayoutRequestedEventConsumer(
            WalletRepository walletRepository,
            WalletTransactionRepository walletTransactionRepository,
            IdentifierGenerator identifierGenerator,
            TimeProvider timeProvider,
            KafkaSellerSettlementPayoutResultEventPublisher payoutResultEventPublisher,
            ObjectMapper objectMapper
    ) {
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.identifierGenerator = identifierGenerator;
        this.timeProvider = timeProvider;
        this.payoutResultEventPublisher = payoutResultEventPublisher;
        this.objectMapper = objectMapper;
    }

    /**
     * 지급 요청 이벤트를 처리하고 결과를 payment -> settlement 이벤트로 발행한다.
     * <p>
     * - 비재시도 비즈니스 실패(예: WALLET_NOT_FOUND)는 FAILED 결과 이벤트로 반영한다.
     * - 그 외 예외는 Kafka 에러 처리기로 전파해 retry(재시도)/DLQ(사후처리큐) 정책에 위임한다.
     */
    @KafkaListener(
            topics = KafkaTopics.SETTLEMENT_PAYOUT_REQUESTED,
            groupId = KafkaConsumerGroups.PAYMENT_SERVICE,
            containerFactory = "sellerSettlementPayoutRequestedKafkaListenerContainerFactory"
    )
    public void listen(String eventJson) {
        EventEnvelope<SellerSettlementPayoutRequestedMessage> envelope = readEnvelope(eventJson);
        validateEnvelope(envelope);
        SellerSettlementPayoutRequestedMessage event = envelope.payload();

        // 이벤트 검증
        validateEvent(event);

        // 시간 일관성을 위해 이벤트 처리 시작 시점의 시간을 기준으로 사용한다.
        LocalDateTime now = timeProvider.now();
        try {
            WalletTransaction existingTransaction = walletTransactionRepository
                    .findByReferenceIdAndReferenceType(event.settlementId(), resolveSettlementReferenceType(event.settlementType()))
                    .orElse(null);
            // 이미 동일한 settlementId로 지급 처리된 거래가 있으면 중복 처리 방지 위해 성공으로 간주하고 종료한다.
            // settlement 서비스가 상태를 맞출 수 있도록 성공 결과 이벤트는 항상 발행.
            if (existingTransaction != null) {
                publishSuccess(event, now);
                return;
            }
            // 판매자 지갑이 있는지 검증
            Wallet wallet = walletRepository.findByMemberId(event.sellerMemberId())
                    .orElseThrow(WalletNotFoundException::new);

            // 증가 후 잔액(balanceAfter)을 반환해 거래 이력에 스냅샷처럼 남긴다.
            java.math.BigDecimal balanceAfter = wallet.increaseBalance(event.payoutAmount(), now);
            walletRepository.save(wallet);

            // 예치금이 변경되어 기록을 남긴다.
            WalletTransaction settlementTransaction = WalletTransaction.create(
                    identifierGenerator.generateUuid(),
                    wallet.getWalletId(),
                    event.payoutAmount(),
                    balanceAfter,
                    WalletTransactionType.SETTLEMENT,
                    event.settlementId(),
                    resolveSettlementReferenceType(event.settlementType()),
                    resolveSettlementDescription(event.settlementType()),
                    now
            );
            walletTransactionRepository.save(settlementTransaction);

            // 성공 이벤트를 발행
            publishSuccess(event, now);
        } catch (WalletNotFoundException e) {
            log.error("[PayoutFailure] settlementId={} reason={}", event.settlementId(), PayoutFailureReason.WALLET_NOT_FOUND);
            // 지갑이 없어서 실패한 경우 재시도 해도 의미 없으므로 재시도하지 않게 실패 이벤트를 발행
            publishFailure(event, now);
        } catch (RuntimeException e) {
            // RETRYABLE 오류는 Kafka 에러 처리기에서 재시도/백오프를 수행하도록 전파한다.
            log.warn("[PayoutRetryDelegated] settlementId={} message={}", event.settlementId(), e.getMessage(), e);
            throw e;
        }
    }

    private EventEnvelope<SellerSettlementPayoutRequestedMessage> readEnvelope(String eventJson) {
        try {
            return objectMapper.readValue(eventJson, SELLER_SETTLEMENT_PAYOUT_REQUESTED_ENVELOPE_TYPE);
        } catch (Exception e) {
            log.error("Failed to deserialize SellerSettlementPayoutRequested event envelope", e);
            throw new RuntimeException("Failed to deserialize SellerSettlementPayoutRequested event envelope", e);
        }
    }

    private void validateEnvelope(EventEnvelope<SellerSettlementPayoutRequestedMessage> envelope) {
        Objects.requireNonNull(envelope, "sellerSettlementPayoutRequested envelope is required.");
        if (envelope.eventId() == null) {
            throw new IllegalArgumentException("eventId is required.");
        }
        if (!SELLER_SETTLEMENT_PAYOUT_REQUESTED_EVENT_TYPE.equals(envelope.eventType())) {
            throw new IllegalArgumentException("Unsupported eventType: " + envelope.eventType());
        }
        if (envelope.source() == null || envelope.source().isBlank()) {
            throw new IllegalArgumentException("source is required.");
        }
        if (envelope.aggregateId() == null) {
            throw new IllegalArgumentException("aggregateId is required.");
        }
        if (envelope.occurredAt() == null) {
            throw new IllegalArgumentException("occurredAt is required.");
        }
        if (envelope.traceId() == null || envelope.traceId().isBlank()) {
            throw new IllegalArgumentException("traceId is required.");
        }
        if (envelope.payload() == null) {
            throw new IllegalArgumentException("payload is required.");
        }
        if (!Objects.equals(envelope.aggregateId(), envelope.payload().settlementId())) {
            throw new IllegalArgumentException("aggregateId and payload.settlementId must match.");
        }
        if (envelope.recipientId() != null
                && !Objects.equals(envelope.recipientId(), envelope.payload().sellerMemberId())) {
            throw new IllegalArgumentException("recipientId and payload.sellerMemberId must match.");
        }
    }

    /**
     * wallet 반영이 정상 완료된 경우 SUCCESS 결과 이벤트를 settlement로 발행한다.
     */
    private void publishSuccess(SellerSettlementPayoutRequestedMessage event, LocalDateTime processedAt) {
        payoutResultEventPublisher.publish(new SellerSettlementPayoutResultMessage(
                identifierGenerator.generateUuid(),
                event.eventId(),
                event.settlementId(),
                event.sellerMemberId(),
                event.payoutAmount(),
                SellerSettlementPayoutResultStatus.SUCCESS,
                null,
                processedAt
        ));
    }

    /**
     * wallet 미존재처럼 즉시 실패로 확정 가능한 경우 FAILED 결과 이벤트를 settlement로 발행한다.
     * 이 consumer에서는 현재 {@code WALLET_NOT_FOUND}만 비재시도 실패로 매핑한다.
     * <p>
     * settlement 모듈은 이 이벤트를 수신하여 settlement 상태를 FAILED로 변경하고,
     * failureReason을 저장한다. NON_RETRYABLE 실패는 수동 조치 대상으로 플래그된다.
     */
    private void publishFailure(SellerSettlementPayoutRequestedMessage event, LocalDateTime processedAt) {
        payoutResultEventPublisher.publish(new SellerSettlementPayoutResultMessage(
                identifierGenerator.generateUuid(),
                event.eventId(),
                event.settlementId(),
                event.sellerMemberId(),
                event.payoutAmount(),
                SellerSettlementPayoutResultStatus.FAILED,
                PayoutFailureReason.WALLET_NOT_FOUND,
                processedAt
        ));
    }

    /**
     * settlement payout 요청 이벤트의 필수 필드와 기본 형식을 검증한다.
     */
    private void validateEvent(SellerSettlementPayoutRequestedMessage event) {
        Objects.requireNonNull(event, "sellerSettlementPayoutRequested event is required.");
        if (event.eventId() == null) {
            throw new IllegalArgumentException("eventId is required.");
        }
        if (event.settlementId() == null) {
            throw new IllegalArgumentException("settlementId is required.");
        }
        if (event.sellerMemberId() == null) {
            throw new IllegalArgumentException("sellerMemberId is required.");
        }
        if (event.settlementType() == null) {
            throw new IllegalArgumentException("settlementType is required.");
        }
        if (event.payoutAmount() == null || event.payoutAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("payoutAmount must be positive.");
        }
        if (event.requestedAt() == null) {
            throw new IllegalArgumentException("requestedAt is required.");
        }
    }

    private String resolveSettlementReferenceType(SettlementPayoutType settlementPayoutType) {
        return switch (settlementPayoutType) {
            case MONTHLY -> "MONTHLY_SETTLEMENT";
            case PARTIAL -> "PARTIAL_SETTLEMENT";
        };
    }

    private String resolveSettlementDescription(SettlementPayoutType settlementPayoutType) {
        return switch (settlementPayoutType) {
            case MONTHLY -> "monthly settlement payout";
            case PARTIAL -> "partial settlement payout";
        };
    }
}

