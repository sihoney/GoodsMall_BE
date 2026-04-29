package com.example.settlement.infrastructure.repository;

import com.example.settlement.domain.entity.Settlement;
import com.example.settlement.domain.enumtype.SettlementStatus;
import com.example.settlement.domain.enumtype.SettlementType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
            select settlement
              from Settlement settlement
             where settlement.sellerId = :sellerId
               and (:settlementType is null or settlement.settlementType = :settlementType)
               and (:settlementStatus is null or settlement.settlementStatus = :settlementStatus)
               and (:settlementYear is null or settlement.settlementYear = :settlementYear)
               and (:settlementMonth is null or settlement.settlementMonth = :settlementMonth)
            """)
    Page<Settlement> findBySellerIdWithFilters(
            @Param("sellerId") UUID sellerId,
            @Param("settlementType") SettlementType settlementType,
            @Param("settlementStatus") SettlementStatus settlementStatus,
            @Param("settlementYear") Integer settlementYear,
            @Param("settlementMonth") Integer settlementMonth,
            Pageable pageable
    );

    Optional<Settlement> findBySettlementIdAndSellerId(UUID settlementId, UUID sellerId);
}
