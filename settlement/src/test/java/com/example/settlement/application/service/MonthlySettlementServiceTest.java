package com.example.settlement.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.settlement.application.dto.MonthlySettlementAggregateCommand;
import com.example.settlement.application.dto.MonthlySettlementAggregateResult;
import com.example.settlement.application.dto.SettlementItemCreateCommand;
import com.example.settlement.domain.entity.Settlement;
import com.example.settlement.domain.entity.SettlementItem;
import com.example.settlement.domain.enumtype.SettlementStatus;
import com.example.settlement.domain.repository.SettlementItemRepository;
import com.example.settlement.domain.repository.SettlementRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MonthlySettlementServiceTest {

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private SettlementItemRepository settlementItemRepository;

    private MonthlySettlementService monthlySettlementService;

    @BeforeEach
    void setUp() {
        monthlySettlementService = new MonthlySettlementService(
                settlementRepository,
                settlementItemRepository
        );
    }

    @Test
    void registerSettlementItemReturnsExistingItemWhenEscrowAlreadyRegistered() {
        UUID escrowId = UUID.randomUUID();
        SettlementItem existingItem = SettlementItem.create(
                UUID.randomUUID(),
                null,
                UUID.randomUUID(),
                escrowId,
                UUID.randomUUID(),
                10_000L,
                1_000L,
                9_000L,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(settlementItemRepository.findByEscrowId(escrowId)).thenReturn(Optional.of(existingItem));

        SettlementItem result = monthlySettlementService.registerSettlementItem(new SettlementItemCreateCommand(
                UUID.randomUUID(),
                escrowId,
                UUID.randomUUID(),
                10_000L,
                LocalDateTime.now()
        ));

        assertThat(result).isSameAs(existingItem);
    }

    @Test
    void registerSettlementItemCreatesSettlementItemWithTenPercentFee() {
        UUID escrowId = UUID.randomUUID();
        when(settlementItemRepository.findByEscrowId(escrowId)).thenReturn(Optional.empty());
        when(settlementItemRepository.save(any(SettlementItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SettlementItem result = monthlySettlementService.registerSettlementItem(new SettlementItemCreateCommand(
                UUID.randomUUID(),
                escrowId,
                UUID.randomUUID(),
                10_000L,
                LocalDateTime.now()
        ));

        assertThat(result.getGrossAmount()).isEqualTo(10_000L);
        assertThat(result.getFeeAmount()).isEqualTo(1_000L);
        assertThat(result.getNetAmount()).isEqualTo(9_000L);
    }

    @ParameterizedTest
    @CsvSource({
            "1,0,1",
            "9,0,9",
            "10,1,9",
            "11,1,10",
            "99,9,90",
            "101,10,91"
    })
    void registerSettlementItemAppliesFloorFeePolicy(long grossAmount, long expectedFeeAmount, long expectedNetAmount) {
        UUID escrowId = UUID.randomUUID();
        when(settlementItemRepository.findByEscrowId(escrowId)).thenReturn(Optional.empty());
        when(settlementItemRepository.save(any(SettlementItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SettlementItem result = monthlySettlementService.registerSettlementItem(new SettlementItemCreateCommand(
                UUID.randomUUID(),
                escrowId,
                UUID.randomUUID(),
                grossAmount,
                LocalDateTime.now()
        ));

        assertThat(result.getFeeAmount()).isEqualTo(expectedFeeAmount);
        assertThat(result.getNetAmount()).isEqualTo(expectedNetAmount);
        assertThat(result.getGrossAmount() - result.getFeeAmount()).isEqualTo(result.getNetAmount());
    }

    @Test
    void aggregateMonthlySettlementsCreatesNewSettlementForFirstItem() {
        UUID sellerId = UUID.randomUUID();
        SettlementItem settlementItem = SettlementItem.create(
                UUID.randomUUID(),
                null,
                UUID.randomUUID(),
                UUID.randomUUID(),
                sellerId,
                10_000L,
                1_000L,
                9_000L,
                LocalDateTime.of(2026, 3, 15, 10, 0),
                LocalDateTime.now()
        );
        Settlement createdSettlement = Settlement.create(
                UUID.randomUUID(),
                sellerId,
                2026,
                3,
                10_000L,
                1_000L,
                9_000L,
                0L,
                SettlementStatus.PENDING,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(settlementItemRepository.findUnassignedByReleasedAtBetween(any(), any())).thenReturn(List.of(settlementItem));
        when(settlementRepository.findBySellerIdAndSettlementYearAndSettlementMonth(sellerId, 2026, 3))
                .thenReturn(Optional.empty());
        when(settlementRepository.save(any(Settlement.class))).thenReturn(createdSettlement);
        when(settlementItemRepository.save(any(SettlementItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MonthlySettlementAggregateResult result = monthlySettlementService.aggregateMonthlySettlements(
                new MonthlySettlementAggregateCommand(
                        2026,
                        3,
                        LocalDateTime.of(2026, 3, 1, 0, 0),
                        LocalDateTime.of(2026, 4, 1, 0, 0)
                )
        );

        assertThat(result.createdSettlementCount()).isEqualTo(1);
        assertThat(result.updatedSettlementCount()).isZero();
        assertThat(result.aggregatedItemCount()).isEqualTo(1);
    }

    @Test
    void aggregateMonthlySettlementsAccumulatesExistingSettlement() {
        UUID sellerId = UUID.randomUUID();
        SettlementItem settlementItem = SettlementItem.create(
                UUID.randomUUID(),
                null,
                UUID.randomUUID(),
                UUID.randomUUID(),
                sellerId,
                20_000L,
                2_000L,
                18_000L,
                LocalDateTime.of(2026, 3, 20, 10, 0),
                LocalDateTime.now()
        );
        Settlement existingSettlement = Settlement.createPending(
                UUID.randomUUID(),
                sellerId,
                2026,
                3,
                10_000L,
                1_000L,
                9_000L,
                LocalDateTime.now()
        );
        when(settlementItemRepository.findUnassignedByReleasedAtBetween(any(), any())).thenReturn(List.of(settlementItem));
        when(settlementRepository.findBySellerIdAndSettlementYearAndSettlementMonth(sellerId, 2026, 3))
                .thenReturn(Optional.of(existingSettlement));
        when(settlementRepository.save(any(Settlement.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(settlementItemRepository.save(any(SettlementItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MonthlySettlementAggregateResult result = monthlySettlementService.aggregateMonthlySettlements(
                new MonthlySettlementAggregateCommand(
                        2026,
                        3,
                        LocalDateTime.of(2026, 3, 1, 0, 0),
                        LocalDateTime.of(2026, 4, 1, 0, 0)
                )
        );

        assertThat(existingSettlement.getTotalSalesAmount()).isEqualTo(30_000L);
        assertThat(existingSettlement.getFeeAmount()).isEqualTo(3_000L);
        assertThat(existingSettlement.getFinalSettlementAmount()).isEqualTo(27_000L);
        assertThat(result.createdSettlementCount()).isZero();
        assertThat(result.updatedSettlementCount()).isEqualTo(1);
        verify(settlementRepository, times(1)).save(existingSettlement);
    }

    @Test
    void aggregatePreviousMonthUsesKstMonthlyBoundary() {
        LocalDateTime referenceDateTime = LocalDateTime.of(2026, 4, 1, 3, 5);
        when(settlementItemRepository.findUnassignedByReleasedAtBetween(
                eq(LocalDateTime.of(2026, 3, 1, 0, 0)),
                eq(LocalDateTime.of(2026, 4, 1, 0, 0))
        )).thenReturn(List.of());

        MonthlySettlementAggregateResult result = monthlySettlementService.aggregatePreviousMonth(referenceDateTime);

        assertThat(result.settlementYear()).isEqualTo(2026);
        assertThat(result.settlementMonth()).isEqualTo(3);
        assertThat(result.aggregatedItemCount()).isZero();
        verify(settlementItemRepository, times(1)).findUnassignedByReleasedAtBetween(
                LocalDateTime.of(2026, 3, 1, 0, 0),
                LocalDateTime.of(2026, 4, 1, 0, 0)
        );
    }

    @Test
    void aggregateMonthlySettlementsSkipsAlreadyAggregatedItems() {
        // given: settlementId가 이미 설정된 집계 완료(aggregated) 항목은 조회 자체에서 제외된다.
        // findUnassignedByReleasedAtBetween이 빈 목록을 반환하면
        // 집계 실행 횟수가 0이어야 한다.
        when(settlementItemRepository.findUnassignedByReleasedAtBetween(any(), any()))
                .thenReturn(List.of());

        MonthlySettlementAggregateResult result = monthlySettlementService.aggregateMonthlySettlements(
                new MonthlySettlementAggregateCommand(
                        2026,
                        3,
                        LocalDateTime.of(2026, 3, 1, 0, 0),
                        LocalDateTime.of(2026, 4, 1, 0, 0)
                )
        );

        assertThat(result.aggregatedItemCount()).isZero();
        assertThat(result.createdSettlementCount()).isZero();
        assertThat(result.updatedSettlementCount()).isZero();
    }

    @Test
    void isAlreadyAggregatedReturnsTrueWhenSettlementIdIsSet() {
        // given: assignSettlement 호출 후 isAlreadyAggregated 가 true를 반환해야 한다.
        UUID settlementId = UUID.randomUUID();
        SettlementItem item = SettlementItem.create(
                UUID.randomUUID(),
                null,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                10_000L,
                1_000L,
                9_000L,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        assertThat(item.isAlreadyAggregated()).isFalse();

        item.assignSettlement(settlementId);

        assertThat(item.isAlreadyAggregated()).isTrue();
    }
}
