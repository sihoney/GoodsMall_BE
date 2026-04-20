package com.example.settlement.infrastructure.repository;

import com.example.settlement.domain.entity.Settlement;
import com.example.settlement.domain.enumtype.SettlementStatus;
import com.example.settlement.domain.enumtype.SettlementType;
import com.example.settlement.domain.repository.SettlementRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class SettlementRepositoryImpl implements SettlementRepository {

    private final SettlementJpaRepository settlementJpaRepository;

    public SettlementRepositoryImpl(SettlementJpaRepository settlementJpaRepository) {
        this.settlementJpaRepository = settlementJpaRepository;
    }

    @Override
    public Settlement save(Settlement settlement) {
        return settlementJpaRepository.save(settlement);
    }

    @Override
    public Optional<Settlement> findBySettlementId(UUID settlementId) {
        return settlementJpaRepository.findBySettlementId(settlementId);
    }

    @Override
    public List<Settlement> findBySettlementYearAndSettlementMonthAndSettlementStatus(
            Integer settlementYear,
            Integer settlementMonth,
            SettlementStatus settlementStatus,
            SettlementType settlementType
    ) {
        return settlementJpaRepository.findBySettlementYearAndSettlementMonthAndSettlementStatus(
                settlementYear,
                settlementMonth,
                settlementStatus,
                settlementType
        );
    }

    @Override
    public Optional<Settlement> findBySellerIdAndSettlementYearAndSettlementMonthAndSettlementType(
            UUID sellerId,
            Integer settlementYear,
            Integer settlementMonth,
            SettlementType settlementType
    ) {
        return settlementJpaRepository.findBySellerIdAndSettlementYearAndSettlementMonthAndSettlementType(
                sellerId,
                settlementYear,
                settlementMonth,
                settlementType
        );
    }

    @Override
    public List<Settlement> findAllBySellerIdInAndSettlementYearAndSettlementMonthAndSettlementType(
            List<UUID> sellerIds,
            Integer settlementYear,
            Integer settlementMonth,
            SettlementType settlementType
    ) {
        return settlementJpaRepository.findAllBySellerIdInAndSettlementYearAndSettlementMonthAndSettlementType(
                sellerIds,
                settlementYear,
                settlementMonth,
                settlementType
        );
    }
}
