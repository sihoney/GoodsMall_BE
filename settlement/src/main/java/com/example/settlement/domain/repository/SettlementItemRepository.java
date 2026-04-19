package com.example.settlement.domain.repository;

import com.example.settlement.domain.entity.SettlementItem;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 정산 원천 항목 repository(저장소) 인터페이스다.
 */
public interface SettlementItemRepository {

    /**
     * 정산 원천 항목을 저장한다.
     */
    SettlementItem save(SettlementItem settlementItem);

    void delete(SettlementItem settlementItem);

    /**
     * escrowId로 정산 원천 항목을 조회한다.
     * dedup(중복 방지) 체크에 사용된다.
     */
    Optional<SettlementItem> findByEscrowId(UUID escrowId);

    /**
     * 지정 기간 내 전체 정산 원천 항목을 조회한다.
     */
    List<SettlementItem> findByReleasedAtBetween(LocalDateTime releasedAtFrom, LocalDateTime releasedAtTo);

    /**
     * 지정 기간 내 아직 월 집계에 포함되지 않은 미집계 항목만 조회한다.
     * settlementId가 null인 항목만 반환하므로 집계 재실행 시 idempotency(멱등성)를 보장한다.
     */
    List<SettlementItem> findUnassignedByReleasedAtBetween(LocalDateTime releasedAtFrom, LocalDateTime releasedAtTo);

    /**
     * 판매자 기준 부분 정산 가능 항목을 조회한다.
     * settlementId가 null이고 grossAmount가 0보다 큰 항목만 반환한다.
     */
    List<SettlementItem> findAvailableSettlementItemsForPartialSettlementBySellerId(UUID sellerId);

    /**
     * settlementItemId 목록으로 정산 원천 항목을 조회한다.
     */
    List<SettlementItem> findAllBySettlementItemIdIn(List<UUID> settlementItemIds);
}
