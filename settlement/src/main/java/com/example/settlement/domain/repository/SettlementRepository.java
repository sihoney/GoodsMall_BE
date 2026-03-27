package com.example.settlement.domain.repository;

import com.example.settlement.domain.entity.Settlement;
import java.util.Optional;
import java.util.UUID;

public interface SettlementRepository {

    Settlement save(Settlement settlement);

    Optional<Settlement> findBySellerIdAndSettlementYearAndSettlementMonth(
            UUID sellerId,
            Integer settlementYear,
            Integer settlementMonth
    );
}
