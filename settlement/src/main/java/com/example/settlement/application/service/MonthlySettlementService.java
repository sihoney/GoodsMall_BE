package com.example.settlement.application.service;

import com.example.settlement.application.dto.MonthlySettlementAggregateCommand;
import com.example.settlement.application.dto.MonthlySettlementAggregateResult;
import com.example.settlement.application.dto.SettlementItemCreateCommand;
import com.example.settlement.application.usecase.MonthlySettlementUseCase;
import com.example.settlement.domain.entity.Settlement;
import com.example.settlement.domain.entity.SettlementItem;
import com.example.settlement.domain.enumtype.SettlementType;
import com.example.settlement.domain.repository.SettlementItemRepository;
import com.example.settlement.domain.repository.SettlementRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 정산 원천 항목 적재와 월 단위 집계를 담당하는 애플리케이션 서비스다.
 */
@Service
@Transactional
public class MonthlySettlementService implements MonthlySettlementUseCase {

    private static final long FEE_RATE_PERCENT = 10L;
    private static final long HUNDRED_PERCENT = 100L;

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
     * payment에서 보낸 에스크로 해제된 정보를 settlement item으로 등록한다.
     */
    @Override
    public SettlementItem registerSettlementItem(SettlementItemCreateCommand command) {
        validateSettlementItemCommand(command);

        // escrowId를 통해서 정산으로 등록되어 있는지 확인후 이미 등록된 항목이 있으면 중복 등록 방지 위해 기존 항목을 반환한다.
        SettlementItem existingItem = settlementItemRepository.findByEscrowId(command.escrowId()).orElse(null);
        if (existingItem != null) {
            return existingItem;
        }

        // 수수료 계산
        BigDecimal feeAmount = calculateFeeAmount(command.grossAmount());
        BigDecimal netAmount = command.grossAmount().subtract(feeAmount);

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
     * 이미 settlementId가 연결된 항목은 조회 단계에서 제외하므로
     * 같은 기간에 집계를 재실행해도 중복 누적이 발생하지 않는다.
     * 이 방식으로 배치(batch) 재실행 시 idempotency(멱등성)를 보장한다.
     */
    public MonthlySettlementAggregateResult aggregateMonthlySettlements(MonthlySettlementAggregateCommand command) {
        validateAggregateCommand(command); // 검증

        List<SettlementItem> unassignedSettlementItems = settlementItemRepository.findUnassignedByReleasedAtBetween(
                command.releasedAtFrom(),
                command.releasedAtTo()
        );
        if (unassignedSettlementItems.isEmpty()) {
            return new MonthlySettlementAggregateResult(
                    command.settlementYear(),
                    command.settlementMonth(),
                    0,
                    0,
                    0
            );
        }

        List<SettlementItem> processingSettlementItems = claimSettlementItemsForMonthlyAggregation(unassignedSettlementItems);
        if (processingSettlementItems.isEmpty()) {
            return new MonthlySettlementAggregateResult(
                    command.settlementYear(),
                    command.settlementMonth(),
                    0,
                    0,
                    0
            );
        }

        Map<UUID, List<SettlementItem>> settlementItemsBySellerId = processingSettlementItems.stream()
                .collect(Collectors.groupingBy(SettlementItem::getSellerId, LinkedHashMap::new, Collectors.toList()));
        Map<UUID, Settlement> monthlySettlementBySellerId = loadExistingMonthlySettlementsBySellerId(
                settlementItemsBySellerId.keySet().stream().toList(),
                command.settlementYear(),
                command.settlementMonth()
        );
        LocalDateTime now = LocalDateTime.now();

        int createdSettlementCount = 0;
        int updatedSettlementCount = 0;
        for (Map.Entry<UUID, List<SettlementItem>> entry : settlementItemsBySellerId.entrySet()) {
            UUID sellerId = entry.getKey();
            List<SettlementItem> sellerSettlementItems = entry.getValue();
            Settlement settlement = monthlySettlementBySellerId.get(sellerId);

            BigDecimal totalGrossAmount = sumGrossAmount(sellerSettlementItems);
            BigDecimal totalFeeAmount = sumFeeAmount(sellerSettlementItems);
            BigDecimal totalNetAmount = sumNetAmount(sellerSettlementItems);

            if (settlement == null) {
                settlement = settlementRepository.save(Settlement.createMonthlyPending(
                        UUID.randomUUID(),
                        sellerId,
                        command.settlementYear(),
                        command.settlementMonth(),
                        totalGrossAmount,
                        totalFeeAmount,
                        totalNetAmount,
                        now
                ));
                monthlySettlementBySellerId.put(sellerId, settlement);
                createdSettlementCount++;
            } else {
                settlement.accumulate(totalGrossAmount, totalFeeAmount, totalNetAmount, now);
                settlementRepository.save(settlement);
                updatedSettlementCount++;
            }

            assignSettlementToItems(settlement, sellerSettlementItems);
        }

        return new MonthlySettlementAggregateResult(
                command.settlementYear(),
                command.settlementMonth(),
                createdSettlementCount,
                updatedSettlementCount,
                processingSettlementItems.size()
        );
    }

    /**
     * 기준 시각의 직전월 기간을 계산해 월 정산 집계를 실행한다.
     */
    @Override
    public MonthlySettlementAggregateResult aggregatePreviousMonth(LocalDateTime referenceDateTime) {
        // null 방어 코드, NullPointerException 발생 예외랑 같은 역할
        Objects.requireNonNull(referenceDateTime, "referenceDateTime must not be null.");
        // minusMonths : 기준 시간보다 한 달뒤로 이동
        // YearMonth : 년, 월 정보만 담는 클래스
        YearMonth targetMonth = YearMonth.from(referenceDateTime.minusMonths(1));
        // 해당 월의 1일을 생성 .atStartOfDay()를 통해 시간은 0시 0분 0초
        LocalDateTime releasedAtFrom = targetMonth.atDay(1).atStartOfDay();
        // 다음달의 월의 1일을 생성
        LocalDateTime releasedAtTo = targetMonth.plusMonths(1).atDay(1).atStartOfDay();
        // 최종적으로 from <= 기간 < to 범위로 집계가 실행된다.
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
        requirePositive(command.grossAmount());
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

    /**
     * MVP 수수료 정책으로 feeAmount를 계산한다.
     * <p>
     * 정책: feeAmount = grossAmount * 10% 이며 소수점 이하는 FLOOR(버림) 처리한다.
     */
    private BigDecimal calculateFeeAmount(BigDecimal grossAmount) {
        return grossAmount.multiply(BigDecimal.valueOf(FEE_RATE_PERCENT))
                .divideToIntegralValue(BigDecimal.valueOf(HUNDRED_PERCENT));
    }

    private void requireUuid(UUID value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null.");
        }
    }

    private void requirePositive(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("grossAmount must be positive.");
        }
    }

    private List<SettlementItem> claimSettlementItemsForMonthlyAggregation(List<SettlementItem> unassignedSettlementItems) {
        List<UUID> settlementItemIds = unassignedSettlementItems.stream()
                .map(SettlementItem::getSettlementItemId)
                .toList();
        int updatedCount = settlementItemRepository.updateSettlementItemStatusIn(
                settlementItemIds,
                com.example.settlement.domain.enumtype.SettlementItemStatus.UNASSIGNED,
                com.example.settlement.domain.enumtype.SettlementItemStatus.PROCESSING
        );
        if (updatedCount == 0) {
            return List.of();
        }
        return settlementItemRepository.findAllBySettlementItemIdInAndSettlementItemStatus(
                settlementItemIds,
                com.example.settlement.domain.enumtype.SettlementItemStatus.PROCESSING
        );
    }

    private Map<UUID, Settlement> loadExistingMonthlySettlementsBySellerId(
            List<UUID> sellerIds,
            int settlementYear,
            int settlementMonth
    ) {
        return settlementRepository.findAllBySellerIdInAndSettlementYearAndSettlementMonthAndSettlementType(
                        sellerIds,
                        settlementYear,
                        settlementMonth,
                        SettlementType.MONTHLY
                ).stream()
                .collect(Collectors.toMap(Settlement::getSellerId, settlement -> settlement, (left, right) -> left, LinkedHashMap::new));
    }

    private void assignSettlementToItems(Settlement settlement, List<SettlementItem> settlementItems) {
        for (SettlementItem settlementItem : settlementItems) {
            settlementItem.assignSettlement(settlement.getSettlementId());
            settlementItemRepository.save(settlementItem);
        }
    }

    private BigDecimal sumGrossAmount(List<SettlementItem> settlementItems) {
        return settlementItems.stream()
                .map(SettlementItem::getGrossAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumFeeAmount(List<SettlementItem> settlementItems) {
        return settlementItems.stream()
                .map(SettlementItem::getFeeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumNetAmount(List<SettlementItem> settlementItems) {
        return settlementItems.stream()
                .map(SettlementItem::getNetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
