package com.example.settlement.infrastructure.repository;

import com.example.settlement.domain.entity.SettlementItem;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementItemJpaRepository extends JpaRepository<SettlementItem, UUID> {

    Optional<SettlementItem> findByEscrowId(UUID escrowId);

    List<SettlementItem> findByReleasedAtGreaterThanEqualAndReleasedAtLessThan(
            LocalDateTime releasedAtFrom,
            LocalDateTime releasedAtTo
    );
}
