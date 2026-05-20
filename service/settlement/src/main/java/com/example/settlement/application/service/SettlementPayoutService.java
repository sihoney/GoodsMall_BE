package com.example.settlement.application.service;

import com.example.settlement.application.dto.FailedPayoutReplayResult;
import com.example.settlement.application.usecase.SettlementPayoutUseCase;
import com.example.settlement.common.exception.CustomException;
import com.example.settlement.common.exception.ErrorCode;
import com.example.settlement.domain.entity.Settlement;
import com.example.settlement.domain.enumtype.SettlementStatus;
import com.example.settlement.domain.enumtype.SettlementType;
import com.example.settlement.domain.repository.SettlementRepository;
import com.example.settlement.infrastructure.messaging.kafka.SettlementPayoutRequestedOutboxEventSaver;
import com.example.settlement.infrastructure.messaging.kafka.contract.PayoutFailureReason;
import com.example.settlement.infrastructure.messaging.kafka.contract.SellerSettlementPayoutRequestedMessage;
import com.example.settlement.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultMessage;
import com.example.settlement.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultStatus;
import com.example.settlement.infrastructure.messaging.kafka.contract.SettlementPayoutType;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
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
public class SettlementPayoutService implements SettlementPayoutUseCase {

    private static final Logger log = LoggerFactory.getLogger(SettlementPayoutService.class);

    private final SettlementRepository settlementRepository;
    private final SettlementPayoutRequestedOutboxEventSaver settlementPayoutRequestedOutboxEventSaver;

    public SettlementPayoutService(
            SettlementRepository settlementRepository,
            SettlementPayoutRequestedOutboxEventSaver settlementPayoutRequestedOutboxEventSaver
    ) {
        this.settlementRepository = settlementRepository;
        this.settlementPayoutRequestedOutboxEventSaver = settlementPayoutRequestedOutboxEventSaver;
    }

    /**
     * 대상 연월의 PENDING 정산건에 대해 지급 요청 이벤트를 발행한다.
     * 흐름:
     * 1) 입력값 검증
     * 2) 해당 연월의 PENDING 정산 목록 조회
     * 3) 각 정산건마다 PROCESSING 상태로 변경 후 지급 요청 이벤트 발행
     * 4) 발행 대상 건수 반환
     * <p>
     * 현재 구현상 특징:
     * - 조회 조건이 PENDING 이므로 아직 지급 요청 전인 건만 대상으로 삼는다.
     * - 상태를 선반영하지 않고 이벤트만 발행하므로, 발행 성공/실패와 DB 상태 정합성은 별도 검토가 필요하다.
     */
    @Override
    public int requestMonthlyPayouts(int settlementYear, int settlementMonth) {
        // 입력값 검증: 연도는 양수, 월은 1~12 범위로 제한한다. 잘못된 입력은 즉시 예외로 차단한다.
        if (settlementYear <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "settlementYear는 1 이상이어야 합니다.");
        }
        if (settlementMonth < 1 || settlementMonth > 12) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "settlementMonth는 1부터 12 사이여야 합니다.");
        }

        // pending 상태의 settlement를 조회
        List<Settlement> pendingSettlements = settlementRepository
                .findBySettlementYearAndSettlementMonthAndSettlementStatus(
                        settlementYear,
                        settlementMonth,
                        SettlementStatus.PENDING,
                        SettlementType.MONTHLY
                );
        // 동일 실행 배치에서 같은 요청 시각을 사용하기 위해 현재 시각을 한 번만 구한다.
        LocalDateTime now = LocalDateTime.now();

        // 조회된 모든 정산건에 대해 지급 요청 이벤트를 발행한다.
        // 각 이벤트는 고유 requestEventId(UUID)를 가져 중복 추적이나 재처리 구분에 활용할 수 있다.
        // 또한 회원당 예치금을 지급 요청하는 방식이므로 for문으로 발행하는 방식 사용
        for (Settlement settlement : pendingSettlements) {
            markSettlementAsProcessingAndPublishPayoutRequest(settlement, now);
        }
        // 실제 발행 시도 건수를 반환한다.
        return pendingSettlements.size();
    }

    @Override
    public void requestPayoutForPartialSettlement(UUID settlementId) {
        if (settlementId == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "settlementId는 필수입니다.");
        }

        Settlement partialSettlement = settlementRepository.findBySettlementId(settlementId)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.SETTLEMENT_NOT_FOUND,
                        "정산 정보를 찾을 수 없습니다. settlementId=" + settlementId
                ));

        if (partialSettlement.getSettlementType() != SettlementType.PARTIAL) {
            throw new CustomException(ErrorCode.INVALID_SETTLEMENT_PAYOUT_REQUEST, "부분 정산만 지급 요청할 수 있습니다.");
        }
        if (partialSettlement.getSettlementStatus() != SettlementStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_SETTLEMENT_PAYOUT_REQUEST, "PENDING 상태의 부분 정산만 지급 요청할 수 있습니다.");
        }
        if (partialSettlement.getFinalSettlementAmount() == null || partialSettlement.getFinalSettlementAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new CustomException(ErrorCode.INVALID_SETTLEMENT_PAYOUT_REQUEST, "부분 정산 지급 금액은 0보다 커야 합니다.");
        }

        markSettlementAsProcessingAndPublishPayoutRequest(partialSettlement, LocalDateTime.now());
    }

    /**
     * payment 모듈의 지급 결과 이벤트를 settlement 상태로 반영한다.
     * 흐름:
     * 1) 이벤트 필수값 검증
     * 2) settlementId로 정산건 조회
     * 3) 성공이면 COMPLETED 반영
     * 4) 실패면 실패 사유를 해석해 FAILED 반영
     * <p>
     * 이벤트 소비 관점:
     * - 동일 성공 이벤트가 중복으로 들어올 수 있으므로 COMPLETED 상태는 no-op 처리한다.
     * - 실패 사유는 재시도 가능 여부 판단 기준으로 사용된다.
     */
    @Override
    public void applyPayoutResult(SellerSettlementPayoutResultMessage event) {
        // 외부 이벤트 검증
        validatePayoutResult(event);

        // 지급 결과를 반영할 실제 정산 엔티티를 조회한다.
        Settlement settlement = settlementRepository.findBySettlementId(event.settlementId())
                .orElseThrow(() -> new CustomException(
                        ErrorCode.SETTLEMENT_NOT_FOUND,
                        "정산 정보를 찾을 수 없습니다. settlementId=" + event.settlementId()
                ));

        // 지급 성공인 경우
        if (event.resultStatus() == SellerSettlementPayoutResultStatus.SUCCESS) {
            applySuccessPayoutResult(settlement, event);
            return;
        }

        // 실패 사유가 누락된 경우 INTERNAL_ERROR로 기본 분류한다.
        // 분류값이 있어야 후속 재시도 정책을 판단할 수 있기 때문이다.
        PayoutFailureReason reason = event.failureReason() != null
                ? event.failureReason()
                : PayoutFailureReason.INTERNAL_ERROR;

        // 재시도 가능 실패인지 여부에 따라 로그 레벨과 운영 메시지를 다르게 남긴다.
        if (reason.isRetryable()) {
            log.warn("[PayoutFailed/RETRYABLE] settlementId={} reason={} requestEventId={}",
                    event.settlementId(), reason, event.requestEventId());
        } else {
            log.error("[PayoutFailed/NON_RETRYABLE] settlementId={} reason={} requestEventId={} — 수동 조치 필요",
                    event.settlementId(), reason, event.requestEventId());
        }
        applyFailedPayoutResult(settlement, event, reason);
    }

    /**
     * 대상 연월의 FAILED 정산건 중 RETRYABLE 실패 사유만 재지급 대상으로 복구해 재요청을 발행한다.
     *
     * 복구 기준:
     * - failureReason 문자열이 enum으로 해석 가능해야 함
     * - 해석된 reason이 retryable 이어야 함
     *
     * 설계 의도:
     * - 자동 재시도는 "재시도 가능한 실패"에만 한정한다.
     * - 사유가 불명확하거나 non-retryable이면 자동화 대상에서 제외한다.
     */
    @Override
    public int requestRetryableFailedPayouts(int settlementYear, int settlementMonth) {
        if (settlementYear <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "settlementYear는 1 이상이어야 합니다.");
        }
        if (settlementMonth < 1 || settlementMonth > 12) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "settlementMonth는 1부터 12 사이여야 합니다.");
        }

        // 해당 연월의 Failed 상태 정산 목록을 조회
        List<Settlement> failedSettlements = settlementRepository
                .findBySettlementYearAndSettlementMonthAndSettlementStatus(
                        settlementYear,
                        settlementMonth,
                        SettlementStatus.FAILED,
                        SettlementType.MONTHLY
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
            markSettlementAsProcessingAndPublishPayoutRequest(settlement, now);
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
    @Override
    public boolean requestManualFailedPayout(UUID settlementId) {
        if (settlementId == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "settlementId는 필수입니다.");
        }

        Settlement settlement = settlementRepository.findBySettlementId(settlementId)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.SETTLEMENT_NOT_FOUND,
                        "정산 정보를 찾을 수 없습니다. settlementId=" + settlementId
                ));

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
        markSettlementAsProcessingAndPublishPayoutRequest(settlement, now);

        log.info("[ManualPayoutRetryRequested] settlementId={} reason={}", settlement.getSettlementId(), reason);
        return true;
    }

    /**
     * settlement를 지급 요청 중 상태로 바꾸고 지급 요청 이벤트를 outbox에 저장한다.
     * <p>
     * 수동 재지급, 자동 재지급, 월 정산 지급 요청이 모두 같은 이벤트 포맷으로 outbox에 적재되도록 공통화한다.
     */
    private void markSettlementAsProcessingAndPublishPayoutRequest(Settlement settlement, LocalDateTime requestedAt) {
        settlement.markPayoutRequested(requestedAt);
        settlementRepository.save(settlement);
        settlementPayoutRequestedOutboxEventSaver.save(new SellerSettlementPayoutRequestedMessage(
                UUID.randomUUID(),
                settlement.getSettlementId(),
                toSettlementPayoutType(settlement.getSettlementType()),
                settlement.getSellerId(),
                settlement.getSettlementYear(),
                settlement.getSettlementMonth(),
                settlement.getFinalSettlementAmount(),
                requestedAt
        ));
    }

    /**
     * payment 모듈에서 넘어온 지급 결과 이벤트의 필수 필드를 검증한다.
     * 잘못된 이벤트는 상태 반영 전에 즉시 예외로 차단한다.
     */
    private void validatePayoutResult(SellerSettlementPayoutResultMessage event) {
        if (event == null) {
            throw new CustomException(ErrorCode.INVALID_SETTLEMENT_PAYOUT_RESULT, "정산 지급 결과 이벤트가 비어 있습니다.");
        }
        if (event.settlementId() == null) {
            throw new CustomException(ErrorCode.INVALID_SETTLEMENT_PAYOUT_RESULT, "settlementId는 필수입니다.");
        }
        if (event.resultStatus() == null) {
            throw new CustomException(ErrorCode.INVALID_SETTLEMENT_PAYOUT_RESULT, "resultStatus는 필수입니다.");
        }
        if (event.processedAt() == null) {
            throw new CustomException(ErrorCode.INVALID_SETTLEMENT_PAYOUT_RESULT, "processedAt은 필수입니다.");
        }
        if (event.payoutAmount() == null || event.payoutAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new CustomException(ErrorCode.INVALID_SETTLEMENT_PAYOUT_RESULT, "payoutAmount는 0보다 커야 합니다.");
        }
    }

    /**
     * 저장된 실패 사유 문자열을 재시도 정책 판단용 enum으로 변환한다.
     * 알 수 없는 값은 자동 재지급 대상에서 제외하기 위해 {@code null}로 취급한다.
     */
    private PayoutFailureReason resolveFailureReason(String failureReason) {
        if (failureReason == null || failureReason.isBlank()) {
            return null;
        }
        try {
            //PayoutFailureReason enum으로 변환
            // 문자열 "INTERNAL_ERROR" -> PayoutFailureReason.INTERNAL_ERROR 로 변환해서 반환
            return PayoutFailureReason.valueOf(failureReason);
        } catch (IllegalArgumentException ignored) {
            // 알 수 없는 코드 값은 재지급 자동화 대상에서 제외한다.
            return null;
        }
    }

    private SettlementPayoutType toSettlementPayoutType(SettlementType settlementType) {
        return switch (settlementType) {
            case MONTHLY -> SettlementPayoutType.MONTHLY;
            case PARTIAL -> SettlementPayoutType.PARTIAL;
        };
    }

    /**
     * 운영자가 확정한 DLQ replay 대상 settlementId 목록을 재처리한다.
     * <p>
     * 분류 기준:
     * - FAILED + RETRYABLE: 자동 재지급 요청 발행
     * - FAILED + NON_RETRYABLE(또는 미분류): 수동 조치 대상으로 집계
     * - FAILED 외 상태: skip 처리
     * - 미존재 settlementId: not found로 집계
     */
    @Override
    public FailedPayoutReplayResult replayFailedPayouts(List<UUID> settlementIds) {
        if (settlementIds == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "settlementIds는 필수입니다.");
        }

        int requestedRetryCount = 0;
        int manualActionRequiredCount = 0;
        int skippedCount = 0;
        int notFoundCount = 0;

        // 서비스가 직접 호출되더라도 중복 ID는 한 번만 처리해 재발행 중복을 방지한다.
        LinkedHashSet<UUID> uniqueSettlementIds = new LinkedHashSet<>();
        for (UUID settlementId : settlementIds) {
            if (settlementId == null) {
                skippedCount++;
                continue;
            }
            uniqueSettlementIds.add(settlementId);
        }

        LocalDateTime now = LocalDateTime.now();
        for (UUID settlementId : uniqueSettlementIds) {
            Settlement settlement = settlementRepository.findBySettlementId(settlementId).orElse(null);
            if (settlement == null) {
                notFoundCount++;
                continue;
            }

            if (settlement.getSettlementStatus() != SettlementStatus.FAILED) {
                skippedCount++;
                continue;
            }

            PayoutFailureReason reason = resolveFailureReason(settlement.getLastFailureReason());
            if (reason != null && reason.isRetryable()) {
                settlement.requeueForPayout(now);
                settlementRepository.save(settlement);
                markSettlementAsProcessingAndPublishPayoutRequest(settlement, now);
                requestedRetryCount++;
                log.info("[DlqReplay/RetryRequested] settlementId={} reason={}", settlement.getSettlementId(), reason);
                continue;
            }

            manualActionRequiredCount++;
            log.warn("[DlqReplay/ManualRequired] settlementId={} reason={}", settlement.getSettlementId(), reason);
        }

        log.info(
                "[DlqReplay/Summary] totalInput={}, uniqueInput={}, retryRequested={}, manualRequired={}, skipped={}, notFound={}",
                settlementIds.size(),
                uniqueSettlementIds.size(),
                requestedRetryCount,
                manualActionRequiredCount,
                skippedCount,
                notFoundCount
        );

        return new FailedPayoutReplayResult(
                requestedRetryCount,
                manualActionRequiredCount,
                skippedCount,
                notFoundCount
        );
    }

    private void applySuccessPayoutResult(Settlement settlement, SellerSettlementPayoutResultMessage event) {
        if (settlement.getSettlementStatus() == SettlementStatus.COMPLETED) {
            log.info("[PayoutSuccessDuplicateIgnored] settlementId={} requestEventId={}",
                    event.settlementId(), event.requestEventId());
            return;
        }

        settlement.complete(event.payoutAmount(), event.processedAt(), LocalDateTime.now());
        settlementRepository.save(settlement);
    }

    private void applyFailedPayoutResult(
            Settlement settlement,
            SellerSettlementPayoutResultMessage event,
            PayoutFailureReason reason
    ) {
        if (settlement.getSettlementStatus() == SettlementStatus.COMPLETED) {
            log.warn("[PayoutFailureIgnoredAfterCompleted] settlementId={} reason={} requestEventId={}",
                    event.settlementId(), reason, event.requestEventId());
            return;
        }
        if (settlement.getSettlementStatus() == SettlementStatus.FAILED) {
            log.info("[PayoutFailureDuplicateIgnored] settlementId={} reason={} requestEventId={}",
                    event.settlementId(), reason, event.requestEventId());
            return;
        }

        settlement.fail(reason.name(), LocalDateTime.now());
        settlementRepository.save(settlement);
    }
}
