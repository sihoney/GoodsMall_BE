package com.example.settlement.domain.repository;

import com.example.settlement.domain.entity.Settlement;
import com.example.settlement.domain.enumtype.SettlementStatus;
import com.example.settlement.domain.enumtype.SettlementType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SettlementRepository {

    Settlement save(Settlement settlement);

    Optional<Settlement> findBySettlementId(UUID settlementId);

    List<Settlement> findBySettlementYearAndSettlementMonthAndSettlementStatus(
            Integer settlementYear,
            Integer settlementMonth,
            SettlementStatus settlementStatus,
            SettlementType settlementType
    );

    Optional<Settlement> findBySellerIdAndSettlementYearAndSettlementMonthAndSettlementType(
            UUID sellerId,
            Integer settlementYear,
            Integer settlementMonth,
            SettlementType settlementType
    );

    List<Settlement> findAllBySellerIdInAndSettlementYearAndSettlementMonthAndSettlementType(
            List<UUID> sellerIds,
            Integer settlementYear,
            Integer settlementMonth,
            SettlementType settlementType
    );
}
