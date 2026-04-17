package com.example.settlement.domain.repository;

import com.example.settlement.domain.entity.SettlementRefundManualAction;
import java.util.Optional;
import java.util.UUID;

public interface SettlementRefundManualActionRepository {

    SettlementRefundManualAction save(SettlementRefundManualAction manualAction);

    Optional<SettlementRefundManualAction> findByRefundIdAndEscrowId(UUID refundId, UUID escrowId);
}
