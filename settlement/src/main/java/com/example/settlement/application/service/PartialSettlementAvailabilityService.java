package com.example.settlement.application.service;

import com.example.settlement.application.dto.PartialSettlementAvailableItemResult;
import com.example.settlement.application.usecase.PartialSettlementAvailabilityUseCase;
import com.example.settlement.domain.entity.SettlementItem;
import com.example.settlement.domain.repository.SettlementItemRepository;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 판매자 부분 정산 가능 항목 조회를 담당하는 서비스다.
 */
@Service
@Transactional(readOnly = true)
public class PartialSettlementAvailabilityService implements PartialSettlementAvailabilityUseCase {

    private final SettlementItemRepository settlementItemRepository;

    public PartialSettlementAvailabilityService(SettlementItemRepository settlementItemRepository) {
        this.settlementItemRepository = settlementItemRepository;
    }

    @Override
    public List<PartialSettlementAvailableItemResult> findAvailableItemsForPartialSettlement(UUID sellerId) {
        Objects.requireNonNull(sellerId, "sellerId must not be null.");

        return settlementItemRepository.findAvailableSettlementItemsForPartialSettlementBySellerId(sellerId).stream()
                .map(this::toPartialSettlementAvailableItemResult)
                .toList();
    }

    private PartialSettlementAvailableItemResult toPartialSettlementAvailableItemResult(SettlementItem settlementItem) {
        return new PartialSettlementAvailableItemResult(
                settlementItem.getSettlementItemId(),
                settlementItem.getEscrowId(),
                settlementItem.getOrderId(),
                settlementItem.getGrossAmount(),
                settlementItem.getFeeAmount(),
                settlementItem.getNetAmount(),
                settlementItem.getReleasedAt()
        );
    }
}
