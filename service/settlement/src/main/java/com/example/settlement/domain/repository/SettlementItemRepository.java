package com.example.settlement.domain.repository;

import com.example.settlement.domain.entity.SettlementItem;
import com.example.settlement.domain.enumtype.SettlementItemStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 정산 원천 항목 repository(저장소) 인터페이스다.
 */
public interface SettlementItemRepository {

    SettlementItem save(SettlementItem settlementItem);

    void delete(SettlementItem settlementItem);

    Optional<SettlementItem> findByEscrowId(UUID escrowId);

    List<SettlementItem> findByReleasedAtBetween(LocalDateTime releasedAtFrom, LocalDateTime releasedAtTo);

    List<SettlementItem> findUnassignedByReleasedAtBetween(LocalDateTime releasedAtFrom, LocalDateTime releasedAtTo);

    List<SettlementItem> findAvailableSettlementItemsForPartialSettlementBySellerId(UUID sellerId);

    List<SettlementItem> findAllBySettlementItemIdIn(List<UUID> settlementItemIds);

    List<SettlementItem> findAllBySettlementItemIdInAndSettlementItemStatus(
            List<UUID> settlementItemIds,
            SettlementItemStatus settlementItemStatus
    );

    List<SettlementItem> findAllBySettlementIdOrderByReleasedAtDesc(UUID settlementId);

    int updateSettlementItemStatusIn(
            List<UUID> settlementItemIds,
            SettlementItemStatus currentStatus,
            SettlementItemStatus nextStatus
    );
}
