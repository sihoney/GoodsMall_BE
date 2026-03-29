package com.example.settlement.infrastructure.repository;

import com.example.settlement.domain.entity.Settlement;
import com.example.settlement.domain.enumtype.SettlementStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementJpaRepository extends JpaRepository<Settlement, UUID> {

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
