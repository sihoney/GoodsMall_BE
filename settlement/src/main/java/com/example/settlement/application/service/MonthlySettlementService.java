package com.example.settlement.application.service;

import com.example.settlement.application.dto.MonthlySettlementAggregateCommand;
import com.example.settlement.application.dto.MonthlySettlementAggregateResult;
import com.example.settlement.application.dto.SettlementItemCreateCommand;
import com.example.settlement.domain.entity.Settlement;
import com.example.settlement.domain.entity.SettlementItem;
import com.example.settlement.domain.repository.SettlementItemRepository;
import com.example.settlement.domain.repository.SettlementRepository;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
/**
 * 정산 원천 항목 적재와 월 단위 집계를 담당하는 애플리케이션 서비스다.
 */
public class MonthlySettlementService {

    private static final long FEE_RATE_PERCENT = 10L;

    private final SettlementRepository settlementRepository;
    private final SettlementItemRepository settlementItemRepository;

    public MonthlySettlementService(
            SettlementRepository settlementRepository,
            SettlementItemRepository settlementItemRepository
    ) {
        this.settlementRepository = settlementRepository;
        this.settlementItemRepository = settlementItemRepository;
    }

    /**
     * payment 원천 이벤트를 월 정산 항목으로 멱등 적재한다.
     */
    public SettlementItem registerSettlementItem(SettlementItemCreateCommand command) {
        validateSettlementItemCommand(command);

        SettlementItem existingItem = settlementItemRepository.findByEscrowId(command.escrowId()).orElse(null);
        if (existingItem != null) {
            return existingItem;
        }

        long feeAmount = calculateFeeAmount(command.grossAmount());
        long netAmount = command.grossAmount() - feeAmount;

        return settlementItemRepository.save(SettlementItem.create(
                UUID.randomUUID(),
                null,
                command.orderId(),
                command.escrowId(),
                command.sellerId(),
                command.grossAmount(),
                feeAmount,
                netAmount,
                command.releasedAt(),
                LocalDateTime.now()
        ));
    }

    /**
     * 지정된 기간의 미집계 정산 원천 항목을 판매자/연월 기준으로 집계해 정산서를 생성 또는 누적 갱신한다.
     *
     * 이미 settlementId가 연결된 항목은 조회 단계에서 제외하므로
     * 같은 기간에 집계를 재실행해도 중복 누적이 발생하지 않는다.
     * 이 방식으로 배치(batch) 재실행 시 idempotency(멱등성)를 보장한다.
     */
    public MonthlySettlementAggregateResult aggregateMonthlySettlements(MonthlySettlementAggregateCommand command) {
        validateAggregateCommand(command);

        // settlementId가 null인 미집계 항목만 조회해 dedup(중복 방지)를 보장한다.
        List<SettlementItem> settlementItems = settlementItemRepository.findUnassignedByReleasedAtBetween(
                command.releasedAtFrom(),
                command.releasedAtTo()
        );

        int createdSettlementCount = 0;
        int updatedSettlementCount = 0;

        for (SettlementItem settlementItem : settlementItems) {
            Settlement settlement = settlementRepository.findBySellerIdAndSettlementYearAndSettlementMonth(
                    settlementItem.getSellerId(),
                    command.settlementYear(),
                    command.settlementMonth()
            ).orElse(null);

            if (settlement == null) {
                settlement = Settlement.createPending(
                        UUID.randomUUID(),
                        settlementItem.getSellerId(),
                        command.settlementYear(),
                        command.settlementMonth(),
                        settlementItem.getGrossAmount(),
                        settlementItem.getFeeAmount(),
                        settlementItem.getNetAmount(),
                        LocalDateTime.now()
                );
                settlement = settlementRepository.save(settlement);
                settlementItem.assignSettlement(settlement.getSettlementId());
                settlementItemRepository.save(settlementItem);
                createdSettlementCount++;
                continue;
            }

            settlement.accumulate(
                    settlementItem.getGrossAmount(),
                    settlementItem.getFeeAmount(),
                    settlementItem.getNetAmount(),
                    LocalDateTime.now()
            );
            settlementRepository.save(settlement);
            settlementItem.assignSettlement(settlement.getSettlementId());
            settlementItemRepository.save(settlementItem);
            updatedSettlementCount++;
        }

        return new MonthlySettlementAggregateResult(
                command.settlementYear(),
                command.settlementMonth(),
                createdSettlementCount,
                updatedSettlementCount,
                settlementItems.size()
        );
    }

    /**
     * 기준 시각의 직전월 기간을 계산해 월 정산 집계를 실행한다.
     */
    public MonthlySettlementAggregateResult aggregatePreviousMonth(LocalDateTime referenceDateTime) {
        Objects.requireNonNull(referenceDateTime, "referenceDateTime must not be null.");

        YearMonth targetMonth = YearMonth.from(referenceDateTime.minusMonths(1));
        LocalDateTime releasedAtFrom = targetMonth.atDay(1).atStartOfDay();
        LocalDateTime releasedAtTo = targetMonth.plusMonths(1).atDay(1).atStartOfDay();

        return aggregateMonthlySettlements(new MonthlySettlementAggregateCommand(
                targetMonth.getYear(),
                targetMonth.getMonthValue(),
                releasedAtFrom,
                releasedAtTo
        ));
    }

    private void validateSettlementItemCommand(SettlementItemCreateCommand command) {
        Objects.requireNonNull(command, "command must not be null.");
        requireUuid(command.orderId(), "orderId");
        requireUuid(command.escrowId(), "escrowId");
        requireUuid(command.sellerId(), "sellerId");
        requirePositive(command.grossAmount(), "grossAmount");
        Objects.requireNonNull(command.releasedAt(), "releasedAt must not be null.");
    }

    private void validateAggregateCommand(MonthlySettlementAggregateCommand command) {
        Objects.requireNonNull(command, "command must not be null.");
        if (command.settlementYear() <= 0) {
            throw new IllegalArgumentException("settlementYear must be positive.");
        }
        if (command.settlementMonth() < 1 || command.settlementMonth() > 12) {
            throw new IllegalArgumentException("settlementMonth must be between 1 and 12.");
        }
        Objects.requireNonNull(command.releasedAtFrom(), "releasedAtFrom must not be null.");
        Objects.requireNonNull(command.releasedAtTo(), "releasedAtTo must not be null.");
        if (!command.releasedAtFrom().isBefore(command.releasedAtTo())) {
            throw new IllegalArgumentException("releasedAtFrom must be before releasedAtTo.");
        }
    }

    private long calculateFeeAmount(long grossAmount) {
        return grossAmount * FEE_RATE_PERCENT / 100;
    }

    private void requireUuid(UUID value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null.");
        }
    }

    private void requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive.");
        }
    }
}
