package com.example.settlement.domain.repository;

import com.example.settlement.domain.entity.Settlement;
import com.example.settlement.domain.enumtype.SettlementStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SettlementRepository {

    Settlement save(Settlement settlement);

    Optional<Settlement> findBySettlementId(UUID settlementId);

    List<Settlement> findBySettlementYearAndSettlementMonthAndSettlementStatus(
            Integer settlementYear,
            Integer settlementMonth,
            SettlementStatus settlementStatus
    );

    Optional<Settlement> findBySellerIdAndSettlementYearAndSettlementMonth(
            UUID sellerId,
            Integer settlementYear,
            Integer settlementMonth
    );
}
