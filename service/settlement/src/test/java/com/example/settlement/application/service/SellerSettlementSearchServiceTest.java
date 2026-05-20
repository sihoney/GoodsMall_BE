package com.example.settlement.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.settlement.application.dto.PagedResult;
import com.example.settlement.application.dto.SellerSettlementDetailResult;
import com.example.settlement.application.dto.SellerSettlementListItemResult;
import com.example.settlement.common.exception.CustomException;
import com.example.settlement.domain.entity.Settlement;
import com.example.settlement.domain.entity.SettlementItem;
import com.example.settlement.domain.enumtype.SettlementStatus;
import com.example.settlement.domain.enumtype.SettlementType;
import com.example.settlement.domain.repository.SettlementItemRepository;
import com.example.settlement.domain.repository.SettlementRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class SellerSettlementSearchServiceTest {

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private SettlementItemRepository settlementItemRepository;

    private SellerSettlementSearchService sellerSettlementSearchService;

    @BeforeEach
    void setUp() {
        sellerSettlementSearchService = new SellerSettlementSearchService(
                settlementRepository,
                settlementItemRepository
        );
    }

    @Test
    void findSettlementsReturnsPagedResults() {
        UUID sellerId = UUID.randomUUID();
        Settlement settlement = Settlement.createMonthlyPending(
                UUID.randomUUID(),
                sellerId,
                2026,
                3,
                BigDecimal.valueOf(120000L),
                BigDecimal.valueOf(12000L),
                BigDecimal.valueOf(108000L),
                LocalDateTime.now()
        );
        when(settlementRepository.findBySellerIdWithFilters(
                sellerId,
                SettlementType.MONTHLY,
                SettlementStatus.PENDING,
                2026,
                3,
                PageRequest.of(0, 20, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "requestedAt"))
        )).thenReturn(new PageImpl<>(List.of(settlement)));

        PagedResult<SellerSettlementListItemResult> result = sellerSettlementSearchService.findSettlements(
                sellerId,
                SettlementType.MONTHLY,
                SettlementStatus.PENDING,
                2026,
                3,
                0,
                20
        );

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().settlementType()).isEqualTo(SettlementType.MONTHLY);
        assertThat(result.items().getFirst().settlementStatus()).isEqualTo(SettlementStatus.PENDING);
    }

    @Test
    void findSettlementDetailReturnsSettlementAndItems() {
        UUID sellerId = UUID.randomUUID();
        UUID settlementId = UUID.randomUUID();
        Settlement settlement = Settlement.create(
                settlementId,
                sellerId,
                SettlementType.MONTHLY,
                2026,
                3,
                BigDecimal.valueOf(120000L),
                BigDecimal.valueOf(12000L),
                BigDecimal.valueOf(108000L),
                BigDecimal.valueOf(108000L),
                SettlementStatus.COMPLETED,
                LocalDateTime.now(),
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        SettlementItem item = SettlementItem.create(
                UUID.randomUUID(),
                settlementId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                sellerId,
                BigDecimal.valueOf(50000L),
                BigDecimal.valueOf(5000L),
                BigDecimal.valueOf(45000L),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(settlementRepository.findBySettlementIdAndSellerId(settlementId, sellerId)).thenReturn(Optional.of(settlement));
        when(settlementItemRepository.findAllBySettlementIdOrderByReleasedAtDesc(settlementId)).thenReturn(List.of(item));

        SellerSettlementDetailResult result = sellerSettlementSearchService.findSettlementDetail(sellerId, settlementId);

        assertThat(result.settlementId()).isEqualTo(settlementId);
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().grossAmount()).isEqualTo(BigDecimal.valueOf(50000L));
    }

    @Test
    void findSettlementDetailThrowsWhenSettlementNotFound() {
        UUID sellerId = UUID.randomUUID();
        UUID settlementId = UUID.randomUUID();
        when(settlementRepository.findBySettlementIdAndSellerId(settlementId, sellerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerSettlementSearchService.findSettlementDetail(sellerId, settlementId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("정산 정보를 찾을 수 없습니다.");
    }
}
