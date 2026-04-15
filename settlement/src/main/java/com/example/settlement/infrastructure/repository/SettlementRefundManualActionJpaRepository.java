package com.example.settlement.infrastructure.repository;

import com.example.settlement.domain.entity.SettlementRefundManualAction;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRefundManualActionJpaRepository extends JpaRepository<SettlementRefundManualAction, UUID> {

    Optional<SettlementRefundManualAction> findByRefundIdAndEscrowId(UUID refundId, UUID escrowId);
}
