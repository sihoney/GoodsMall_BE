package com.example.settlement.infrastructure.repository;

import com.example.settlement.domain.entity.SettlementItem;
import com.example.settlement.domain.enumtype.SettlementItemStatus;
import com.example.settlement.domain.repository.SettlementItemRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * SettlementItemRepository 구현체다.
 * JPA repository(저장소)에 위임해 실제 DB 접근을 처리한다.
 */
@Repository
public class SettlementItemRepositoryImpl implements SettlementItemRepository {

    private final SettlementItemJpaRepository settlementItemJpaRepository;

    public SettlementItemRepositoryImpl(SettlementItemJpaRepository settlementItemJpaRepository) {
        this.settlementItemJpaRepository = settlementItemJpaRepository;
    }

    /**
     * 정산 원천 항목을 저장한다.
     */
    @Override
    public SettlementItem save(SettlementItem settlementItem) {
        return settlementItemJpaRepository.save(settlementItem);
    }

    @Override
    public void delete(SettlementItem settlementItem) {
        settlementItemJpaRepository.delete(settlementItem);
    }

    /**
     * escrowId로 단건 조회한다. dedup(중복 방지) 체크에 사용된다.
     */
    @Override
    public Optional<SettlementItem> findByEscrowId(UUID escrowId) {
        return settlementItemJpaRepository.findByEscrowId(escrowId);
    }

    /**
     * 지정 기간 내 전체 항목을 조회한다. [from, to) 반열린 구간으로 조회한다.
     */
    @Override
    public List<SettlementItem> findByReleasedAtBetween(LocalDateTime releasedAtFrom, LocalDateTime releasedAtTo) {
        return settlementItemJpaRepository.findByReleasedAtGreaterThanEqualAndReleasedAtLessThan(
                releasedAtFrom,
                releasedAtTo
        );
    }

    /**
     * 지정 기간 내 UNASSIGNED 항목만 조회한다.
     */
    @Override
    public List<SettlementItem> findUnassignedByReleasedAtBetween(
            LocalDateTime releasedAtFrom,
            LocalDateTime releasedAtTo
    ) {
        return settlementItemJpaRepository
                .findBySettlementItemStatusAndReleasedAtGreaterThanEqualAndReleasedAtLessThan(
                        SettlementItemStatus.UNASSIGNED,
                        releasedAtFrom,
                        releasedAtTo
                );
    }

    @Override
    public List<SettlementItem> findAvailableSettlementItemsForPartialSettlementBySellerId(UUID sellerId) {
        return settlementItemJpaRepository.findBySellerIdAndSettlementItemStatusAndGrossAmountGreaterThanOrderByReleasedAtDesc(
                sellerId,
                SettlementItemStatus.UNASSIGNED,
                0L
        );
    }

    @Override
    public List<SettlementItem> findAllBySettlementItemIdIn(List<UUID> settlementItemIds) {
        return settlementItemJpaRepository.findBySettlementItemIdIn(settlementItemIds);
    }
}
