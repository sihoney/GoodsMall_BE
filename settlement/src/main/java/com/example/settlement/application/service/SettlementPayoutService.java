package com.example.settlement.application.service;

import com.example.settlement.domain.entity.Settlement;
import com.example.settlement.domain.enumtype.SettlementStatus;
import com.example.settlement.domain.repository.SettlementRepository;
import com.example.settlement.infrastructure.messaging.kafka.KafkaSellerSettlementPayoutRequestedEventPublisher;
import com.example.settlement.infrastructure.messaging.kafka.contract.PayoutFailureReason;
import com.example.settlement.infrastructure.messaging.kafka.contract.SellerSettlementPayoutRequestedMessage;
import com.example.settlement.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultMessage;
import com.example.settlement.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 정산 지급 요청 발행과 지급 결과 반영을 담당하는 애플리케이션 서비스다.
 */
@Service
@Transactional
public class SettlementPayoutService {

    private static final Logger log = LoggerFactory.getLogger(SettlementPayoutService.class);

    private final SettlementRepository settlementRepository;
    private final KafkaSellerSettlementPayoutRequestedEventPublisher payoutRequestedEventPublisher;

    public SettlementPayoutService(
            SettlementRepository settlementRepository,
            KafkaSellerSettlementPayoutRequestedEventPublisher payoutRequestedEventPublisher
    ) {
        this.settlementRepository = settlementRepository;
        this.payoutRequestedEventPublisher = payoutRequestedEventPublisher;
    }

    /**
     * 대상 연월의 PENDING 정산건에 대해 지급 요청 이벤트를 발행한다.
     */
    public int requestMonthlyPayouts(int settlementYear, int settlementMonth) {
        if (settlementYear <= 0) {
            throw new IllegalArgumentException("settlementYear must be positive.");
        }
        if (settlementMonth < 1 || settlementMonth > 12) {
            throw new IllegalArgumentException("settlementMonth must be between 1 and 12.");
        }

        List<Settlement> pendingSettlements = settlementRepository
                .findBySettlementYearAndSettlementMonthAndSettlementStatus(
                        settlementYear,
                        settlementMonth,
                        SettlementStatus.PENDING
                );

        LocalDateTime now = LocalDateTime.now();
        for (Settlement settlement : pendingSettlements) {
            payoutRequestedEventPublisher.publish(new SellerSettlementPayoutRequestedMessage(
                    UUID.randomUUID(),
                    settlement.getSettlementId(),
                    settlement.getSellerId(),
                    settlement.getSettlementYear(),
                    settlement.getSettlementMonth(),
                    settlement.getFinalSettlementAmount(),
                    now
            ));
        }

        return pendingSettlements.size();
    }

    /**
     * payment 모듈의 지급 결과 이벤트를 settlement 상태로 반영한다.
     */
    public void applyPayoutResult(SellerSettlementPayoutResultMessage event) {
        validatePayoutResult(event);

        Settlement settlement = settlementRepository.findBySettlementId(event.settlementId())
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found: " + event.settlementId()));

        if (event.resultStatus() == SellerSettlementPayoutResultStatus.SUCCESS) {
            // 이미 COMPLETED 상태면 중복 성공 이벤트로 보고 no-op(아무 작업 없음) 처리한다.
            if (settlement.getSettlementStatus() == SettlementStatus.COMPLETED) {
                return;
            }
            settlement.complete(event.payoutAmount(), event.processedAt(), LocalDateTime.now());
            settlementRepository.save(settlement);
            return;
        }

        PayoutFailureReason reason = event.failureReason() != null
                ? event.failureReason()
                : PayoutFailureReason.INTERNAL_ERROR;

        if (reason.isRetryable()) {
            log.warn("[PayoutFailed/RETRYABLE] settlementId={} reason={} requestEventId={}",
                    event.settlementId(), reason, event.requestEventId());
        } else {
            log.error("[PayoutFailed/NON_RETRYABLE] settlementId={} reason={} requestEventId={} — 수동 조치 필요",
                    event.settlementId(), reason, event.requestEventId());
        }

        settlement.fail(reason.name(), LocalDateTime.now());
        settlementRepository.save(settlement);
    }

    private void validatePayoutResult(SellerSettlementPayoutResultMessage event) {
        Objects.requireNonNull(event, "sellerSettlementPayoutResult event is required.");
        if (event.settlementId() == null) {
            throw new IllegalArgumentException("settlementId is required.");
        }
        if (event.resultStatus() == null) {
            throw new IllegalArgumentException("resultStatus is required.");
        }
        if (event.processedAt() == null) {
            throw new IllegalArgumentException("processedAt is required.");
        }
        if (event.payoutAmount() == null || event.payoutAmount() <= 0) {
            throw new IllegalArgumentException("payoutAmount must be positive.");
        }
    }
}

