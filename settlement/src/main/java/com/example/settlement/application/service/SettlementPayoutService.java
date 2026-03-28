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

    /**
     * 대상 연월의 FAILED 정산건 중 RETRYABLE 실패 사유만 재지급 대상으로 복구해 재요청을 발행한다.
     * <p>
     * 복구 기준:
     * - failureReason이 PayoutFailureReason으로 파싱 가능
     * - reason.isRetryable() == true
     */
    public int requestRetryableFailedPayouts(int settlementYear, int settlementMonth) {
        if (settlementYear <= 0) {
            throw new IllegalArgumentException("settlementYear must be positive.");
        }
        if (settlementMonth < 1 || settlementMonth > 12) {
            throw new IllegalArgumentException("settlementMonth must be between 1 and 12.");
        }

        List<Settlement> failedSettlements = settlementRepository
                .findBySettlementYearAndSettlementMonthAndSettlementStatus(
                        settlementYear,
                        settlementMonth,
                        SettlementStatus.FAILED
                );

        LocalDateTime now = LocalDateTime.now();
        int retriedCount = 0;
        for (Settlement settlement : failedSettlements) {
            PayoutFailureReason reason = resolveFailureReason(settlement.getLastFailureReason());
            if (reason == null || !reason.isRetryable()) {
                continue;
            }

            settlement.requeueForPayout(now);
            settlementRepository.save(settlement);
            publishPayoutRequest(settlement, now);
            retriedCount++;

            log.info("[PayoutRetryRequested] settlementId={} reason={}", settlement.getSettlementId(), reason);
        }

        return retriedCount;
    }

    /**
     * 운영자가 지정한 FAILED 정산건을 수동 재지급 대상으로 복구해 재요청을 발행한다.
     * <p>
     * 수동 재지급은 NON_RETRYABLE 또는 알 수 없는 실패 사유에만 허용한다.
     * RETRYABLE 실패 사유는 자동 재시도 오케스트레이션 경로를 사용한다.
     */
    public boolean requestManualFailedPayout(UUID settlementId) {
        Objects.requireNonNull(settlementId, "settlementId is required.");

        Settlement settlement = settlementRepository.findBySettlementId(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found: " + settlementId));

        if (settlement.getSettlementStatus() != SettlementStatus.FAILED) {
            log.warn("[ManualPayoutRetrySkipped] settlementId={} status={}",
                    settlement.getSettlementId(), settlement.getSettlementStatus());
            return false;
        }

        PayoutFailureReason reason = resolveFailureReason(settlement.getLastFailureReason());
        if (reason != null && reason.isRetryable()) {
            log.warn("[ManualPayoutRetrySkipped] settlementId={} reason={} auto-retry target",
                    settlement.getSettlementId(), reason);
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        settlement.requeueForPayout(now);
        settlementRepository.save(settlement);
        publishPayoutRequest(settlement, now);

        log.info("[ManualPayoutRetryRequested] settlementId={} reason={}", settlement.getSettlementId(), reason);
        return true;
    }

    /**
     * settlement 지급 요청 이벤트를 공통 형식으로 발행한다.
     */
    private void publishPayoutRequest(Settlement settlement, LocalDateTime requestedAt) {
        payoutRequestedEventPublisher.publish(new SellerSettlementPayoutRequestedMessage(
                UUID.randomUUID(),
                settlement.getSettlementId(),
                settlement.getSellerId(),
                settlement.getSettlementYear(),
                settlement.getSettlementMonth(),
                settlement.getFinalSettlementAmount(),
                requestedAt
        ));
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

    private PayoutFailureReason resolveFailureReason(String failureReason) {
        if (failureReason == null || failureReason.isBlank()) {
            return null;
        }
        try {
            return PayoutFailureReason.valueOf(failureReason);
        } catch (IllegalArgumentException ignored) {
            // 알 수 없는 코드 값은 재지급 자동화 대상에서 제외한다.
            return null;
        }
    }
}

