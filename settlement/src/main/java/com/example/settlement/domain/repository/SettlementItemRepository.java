package com.example.settlement.domain.repository;

import com.example.settlement.domain.entity.SettlementItem;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SettlementItemRepository {

    SettlementItem save(SettlementItem settlementItem);

    Optional<SettlementItem> findByEscrowId(UUID escrowId);

    List<SettlementItem> findByReleasedAtBetween(LocalDateTime releasedAtFrom, LocalDateTime releasedAtTo);
}
