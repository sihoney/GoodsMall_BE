package com.example.settlement.infrastructure.repository;

import com.example.settlement.domain.entity.SettlementItem;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * SettlementItem JPA repository(저장소)다.
 * Spring Data JPA 쿼리 메서드를 통해 DB 조회를 담당한다.
 */
public interface SettlementItemJpaRepository extends JpaRepository<SettlementItem, UUID> {

    /**
     * escrowId로 단건 조회한다. dedup(중복 방지) 체크에 사용된다.
     */
    Optional<SettlementItem> findByEscrowId(UUID escrowId);

    /**
     * 지정 기간 내 전체 항목을 조회한다. [from, to) 반열린 구간으로 조회한다.
     */
    List<SettlementItem> findByReleasedAtGreaterThanEqualAndReleasedAtLessThan(
            LocalDateTime releasedAtFrom,
            LocalDateTime releasedAtTo
    );

    /**
     * 지정 기간 내 settlementId가 null인 미집계 항목만 조회한다.
     * 집계 재실행 시 이미 처리된 항목을 skip(건너뜀)해 idempotency(멱등성)를 보장한다.
     */
    List<SettlementItem> findBySettlementIdIsNullAndReleasedAtGreaterThanEqualAndReleasedAtLessThan(
            LocalDateTime releasedAtFrom,
            LocalDateTime releasedAtTo
    );

    /**
     * 판매자 기준 부분 정산 가능 항목을 최신 정산일 순으로 조회한다.
     */
    List<SettlementItem> findBySellerIdAndSettlementIdIsNullAndGrossAmountGreaterThanOrderByReleasedAtDesc(
            UUID sellerId,
            Long grossAmount
    );
}
