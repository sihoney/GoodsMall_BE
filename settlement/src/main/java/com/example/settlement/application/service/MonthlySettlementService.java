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
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
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
     * 이미 settlementId가 연결된 항목은 조회 단계에서 제외하므로
     * 같은 기간에 집계를 재실행해도 중복 누적이 발생하지 않는다.
     * 이 방식으로 배치(batch) 재실행 시 idempotency(멱등성)를 보장한다.
     */
    public MonthlySettlementAggregateResult aggregateMonthlySettlements(MonthlySettlementAggregateCommand command) {
        validateAggregateCommand(command); // 검증

        // settlementId가 null인 미집계 항목만 조회해 dedup(중복 방지)를 보장한다.
        List<SettlementItem> settlementItems = settlementItemRepository.findUnassignedByReleasedAtBetween(
                command.releasedAtFrom(),
                command.releasedAtTo()
        );

        int createdSettlementCount = 0; // 새로 생성된 정산서 개수 파악
        int updatedSettlementCount = 0; // 기존 정산서 업데이트 개수 파악

        // todo: 반복문 관련 잦은 save, search는 성능에 영향을 줄 수 있으니 최적화 방법 고려
        for (SettlementItem settlementItem : settlementItems) {
            // 년, 월 기준으로 이미 만들어진 정산서가 있는지 조회
            // todo: N+1 조회 가능성이 있으므로 향후 최적화 필요.
            //  예를 들어, 집계 대상 항목들의 판매자 ID와 연/월 기준으로 미리 정산서를 조회해 캐싱하는 방식으로 개선할 수 있다.
            Settlement settlement = settlementRepository.findBySellerIdAndSettlementYearAndSettlementMonthAndSettlementType(
                    settlementItem.getSellerId(),
                    command.settlementYear(),
                    command.settlementMonth(),
                    SettlementType.MONTHLY
            ).orElse(null);

            // todo: 동시성 문제 해결 필요.
            //  여러 집계 작업이 동시에 실행될 때 같은 판매자/연월에 대해 중복으로 정산서가 생성될 수 있다.
            //  (sellerId, year, month) 유니크 제약 있는지 확인 필요. 없다면 낙관적 락이나 PESSIMISTIC_WRITE 락 고려.
            // 정산서가 없을 경우 새롭게 생성
            if (settlement == null) {
                // 정산서를 Pending 상태로 생성한다. 집계가 완료된 후에야 확정 상태로 변경.
                settlement = Settlement.createMonthlyPending(
                        UUID.randomUUID(),
                        settlementItem.getSellerId(),
                        command.settlementYear(),
                        command.settlementMonth(),
                        settlementItem.getGrossAmount(),
                        settlementItem.getFeeAmount(),
                        settlementItem.getNetAmount(),
                        // todo: 루프안에서 now는 미세한 시간 차이를 발생 시킬 수 있다.
                        //  시간 차이에 대해서 상관 있는지 없는지 체크할 것.
                        LocalDateTime.now()
                );
                // 새로 만든 정산서 저장
                settlement = settlementRepository.save(settlement);
                // 만들어진 정산 Id를 정산 아이템 테이블과 연결
                settlementItem.assignSettlement(settlement.getSettlementId());
                settlementItemRepository.save(settlementItem);
                createdSettlementCount++;
                continue;
            }
            // 이미 정산서가 있으면 금액을 누적한다.
            settlement.accumulate(
                    settlementItem.getGrossAmount(),
                    settlementItem.getFeeAmount(),
                    settlementItem.getNetAmount(),
                    // todo: 루프안에서 now는 미세한 시간 차이를 발생 시킬 수 있다.
                    LocalDateTime.now()
            );
            settlementRepository.save(settlement);
            settlementItem.assignSettlement(settlement.getSettlementId());
            settlementItemRepository.save(settlementItem);
            updatedSettlementCount++;
        }

        // 최종 처리 결과를 반환한다.
        // 어떤 연/월을 집계했는지,
        // 몇 건의 정산서를 새로 만들었는지,
        // 몇 건을 기존 정산서에 누적했는지,
        // 총 몇 개의 원천 항목을 처리했는지 담는다.
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
     * 정책: feeAmount = grossAmount * 10% 이며 Long 정수 나눗셈으로
     * 소수점 이하는 floor(버림) 처리한다.
     */
    private long calculateFeeAmount(long grossAmount) {
        return Math.floorDiv(grossAmount * FEE_RATE_PERCENT, HUNDRED_PERCENT);
    }

    private void requireUuid(UUID value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null.");
        }
    }

    private void requirePositive(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("grossAmount must be positive.");
        }
    }
}
