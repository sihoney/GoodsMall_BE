package com.example.settlement.infrastructure.repository;

import com.example.settlement.domain.entity.SettlementItem;
import com.example.settlement.domain.repository.SettlementItemRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class SettlementItemRepositoryImpl implements SettlementItemRepository {

    private final SettlementItemJpaRepository settlementItemJpaRepository;

    public SettlementItemRepositoryImpl(SettlementItemJpaRepository settlementItemJpaRepository) {
        this.settlementItemJpaRepository = settlementItemJpaRepository;
    }

    @Override
    public SettlementItem save(SettlementItem settlementItem) {
        return settlementItemJpaRepository.save(settlementItem);
    }

    @Override
    public Optional<SettlementItem> findByEscrowId(UUID escrowId) {
        return settlementItemJpaRepository.findByEscrowId(escrowId);
    }

    @Override
    public List<SettlementItem> findByReleasedAtBetween(LocalDateTime releasedAtFrom, LocalDateTime releasedAtTo) {
        return settlementItemJpaRepository.findByReleasedAtGreaterThanEqualAndReleasedAtLessThan(
                releasedAtFrom,
                releasedAtTo
        );
    }
}
