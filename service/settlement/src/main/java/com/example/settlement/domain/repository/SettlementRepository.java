package com.example.settlement.domain.repository;

import com.example.settlement.domain.entity.Settlement;
import com.example.settlement.domain.enumtype.SettlementStatus;
import com.example.settlement.domain.enumtype.SettlementType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    Page<Settlement> findBySellerIdWithFilters(
            UUID sellerId,
            SettlementType settlementType,
            SettlementStatus settlementStatus,
            Integer settlementYear,
            Integer settlementMonth,
            Pageable pageable
    );

    Optional<Settlement> findBySettlementIdAndSellerId(UUID settlementId, UUID sellerId);
}
