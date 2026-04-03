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
import java.time.LocalDateTime;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * settlement -> payment 정산 지급 요청 이벤트를 소비하고 wallet 적립을 처리하는 Kafka consumer(소비기)다.
 */
@Component
@Transactional
public class SellerSettlementPayoutRequestedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(SellerSettlementPayoutRequestedEventConsumer.class);
    private static final String SETTLEMENT_REFERENCE_TYPE = "SETTLEMENT";

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final IdentifierGenerator identifierGenerator;
    private final TimeProvider timeProvider;
    private final KafkaSellerSettlementPayoutResultEventPublisher payoutResultEventPublisher;

    public SellerSettlementPayoutRequestedEventConsumer(
            WalletRepository walletRepository,
            WalletTransactionRepository walletTransactionRepository,
            IdentifierGenerator identifierGenerator,
            TimeProvider timeProvider,
            KafkaSellerSettlementPayoutResultEventPublisher payoutResultEventPublisher
    ) {
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.identifierGenerator = identifierGenerator;
        this.timeProvider = timeProvider;
        this.payoutResultEventPublisher = payoutResultEventPublisher;
    }

    /**
     * 지급 요청 이벤트를 처리하고 결과를 payment -> settlement 이벤트로 발행한다.
     * <p>
     * - 비재시도 비즈니스 실패(예: WALLET_NOT_FOUND)는 FAILED 결과 이벤트로 반영한다.
     * - 그 외 예외는 Kafka 에러 처리기로 전파해 retry(재시도)/DLQ(사후처리큐) 정책에 위임한다.
     */
    @KafkaListener(
            topics = "${payment.kafka.topics.settlement-payout-requested:settlement.seller-payout-requested}",
            groupId = "${payment.kafka.consumer-groups.settlement-payout-requested:payment-service}",
            containerFactory = "sellerSettlementPayoutRequestedKafkaListenerContainerFactory"
    )
    public void listen(SellerSettlementPayoutRequestedMessage event) {
        validateEvent(event);

        LocalDateTime now = timeProvider.now();
        try {
            WalletTransaction existingTransaction = walletTransactionRepository
                    .findByReferenceIdAndReferenceType(event.settlementId(), SETTLEMENT_REFERENCE_TYPE)
                    .orElse(null);
            if (existingTransaction != null) {
                publishSuccess(event, now);
                return;
            }

            Wallet wallet = walletRepository.findByMemberId(event.sellerMemberId())
                    .orElseThrow(WalletNotFoundException::new);

            Long balanceAfter = wallet.increaseBalance(event.payoutAmount(), now);
            walletRepository.save(wallet);

            WalletTransaction settlementTransaction = WalletTransaction.create(
                    identifierGenerator.generateUuid(),
                    wallet.getWalletId(),
                    event.payoutAmount(),
                    balanceAfter,
                    WalletTransactionType.SETTLEMENT,
                    event.settlementId(),
                    SETTLEMENT_REFERENCE_TYPE,
                    "seller settlement payout",
                    now
            );
            walletTransactionRepository.save(settlementTransaction);

            publishSuccess(event, now);
        } catch (WalletNotFoundException e) {
            log.error("[PayoutFailure] settlementId={} reason={}", event.settlementId(), PayoutFailureReason.WALLET_NOT_FOUND);
            publishFailure(event, now);
        } catch (RuntimeException e) {
            // RETRYABLE 오류는 Kafka 에러 처리기에서 재시도/백오프를 수행하도록 전파한다.
            log.warn("[PayoutRetryDelegated] settlementId={} message={}", event.settlementId(), e.getMessage(), e);
            throw e;
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
        if (event.payoutAmount() == null || event.payoutAmount() <= 0) {
            throw new IllegalArgumentException("payoutAmount must be positive.");
        }
        if (event.requestedAt() == null) {
            throw new IllegalArgumentException("requestedAt is required.");
        }
    }
}

