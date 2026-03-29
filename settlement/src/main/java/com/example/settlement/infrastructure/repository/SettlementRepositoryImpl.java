package com.example.settlement.infrastructure.repository;

import com.example.settlement.domain.entity.Settlement;
import com.example.settlement.domain.enumtype.SettlementStatus;
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
            SettlementStatus settlementStatus
    ) {
        return settlementJpaRepository.findBySettlementYearAndSettlementMonthAndSettlementStatus(
                settlementYear,
                settlementMonth,
                settlementStatus
        );
    }

    @Override
    public Optional<Settlement> findBySellerIdAndSettlementYearAndSettlementMonth(
            UUID sellerId,
            Integer settlementYear,
            Integer settlementMonth
    ) {
        return settlementJpaRepository.findBySellerIdAndSettlementYearAndSettlementMonth(
                sellerId,
                settlementYear,
                settlementMonth
        );
    }
}
