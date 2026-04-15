package com.example.settlement.infrastructure.repository;

import com.example.settlement.domain.entity.SettlementRefundManualAction;
import com.example.settlement.domain.repository.SettlementRefundManualActionRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class SettlementRefundManualActionRepositoryImpl implements SettlementRefundManualActionRepository {

    private final SettlementRefundManualActionJpaRepository settlementRefundManualActionJpaRepository;

    public SettlementRefundManualActionRepositoryImpl(
            SettlementRefundManualActionJpaRepository settlementRefundManualActionJpaRepository
    ) {
        this.settlementRefundManualActionJpaRepository = settlementRefundManualActionJpaRepository;
    }

    @Override
    public SettlementRefundManualAction save(SettlementRefundManualAction manualAction) {
        return settlementRefundManualActionJpaRepository.save(manualAction);
    }

    @Override
    public Optional<SettlementRefundManualAction> findByRefundIdAndEscrowId(UUID refundId, UUID escrowId) {
        return settlementRefundManualActionJpaRepository.findByRefundIdAndEscrowId(refundId, escrowId);
    }
}
