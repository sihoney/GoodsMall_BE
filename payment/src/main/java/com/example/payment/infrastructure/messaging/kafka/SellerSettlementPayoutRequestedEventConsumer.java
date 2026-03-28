package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.domain.entity.Wallet;
import com.example.payment.domain.entity.WalletTransaction;
import com.example.payment.domain.enumtype.WalletTransactionType;
import com.example.payment.domain.repository.WalletRepository;
import com.example.payment.domain.repository.WalletTransactionRepository;
import com.example.payment.domain.service.IdentifierGenerator;
import com.example.payment.domain.service.TimeProvider;
import com.example.payment.infrastructure.messaging.kafka.contract.SellerSettlementPayoutRequestedMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultStatus;
import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * settlement -> payment 정산 지급 요청 이벤트를 소비하고 wallet 적립을 처리하는 Kafka consumer(소비기)다.
 */
@Component
@Transactional
public class SellerSettlementPayoutRequestedEventConsumer {

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
                    .orElseThrow(() -> new IllegalArgumentException("Seller wallet not found."));

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
        } catch (RuntimeException e) {
            payoutResultEventPublisher.publish(new SellerSettlementPayoutResultMessage(
                    identifierGenerator.generateUuid(),
                    event.eventId(),
                    event.settlementId(),
                    event.sellerMemberId(),
                    event.payoutAmount(),
                    SellerSettlementPayoutResultStatus.FAILED,
                    e.getMessage(),
                    now
            ));
        }
    }

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

