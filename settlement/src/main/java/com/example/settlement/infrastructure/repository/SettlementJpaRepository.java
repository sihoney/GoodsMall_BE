package com.example.settlement.infrastructure.repository;

import com.example.settlement.domain.entity.Settlement;
import com.example.settlement.domain.enumtype.SettlementStatus;
import com.example.settlement.domain.enumtype.SettlementType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementJpaRepository extends JpaRepository<Settlement, UUID> {

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
