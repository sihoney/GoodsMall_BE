package com.example.settlement.infrastructure.repository;

import com.example.settlement.domain.entity.Settlement;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementJpaRepository extends JpaRepository<Settlement, UUID> {

    Optional<Settlement> findBySellerIdAndSettlementYearAndSettlementMonth(
            UUID sellerId,
            Integer settlementYear,
            Integer settlementMonth
    );
}
