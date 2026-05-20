package com.example.payment.escrow.infrastructure.repository;

import com.example.payment.escrow.domain.entity.Escrow;
import com.example.payment.escrow.domain.enumtype.EscrowReferenceType;
import com.example.payment.escrow.domain.enumtype.EscrowStatus;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

/**
 * escrow JPA ??μ냼??
 * ?ㅼ쨷 seller 二쇰Ц ?댄썑 orderId ?꾩껜 議고쉶? orderId + seller ?④굔 議고쉶瑜?紐⑤몢 吏?먰븳??
 */
public interface EscrowJpaRepository extends JpaRepository<Escrow, UUID> {

    List<Escrow> findAllByOrderId(UUID orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Escrow> findWithLockByOrderId(UUID orderId);

    List<Escrow> findAllByOrderIdAndSellerMemberId(UUID orderId, UUID sellerMemberId);

    List<Escrow> findAllByReferenceTypeAndReferenceIdIn(EscrowReferenceType referenceType, List<UUID> referenceIds);

    Page<Escrow> findBySellerMemberIdAndEscrowStatus(UUID sellerMemberId, EscrowStatus escrowStatus, Pageable pageable);
}
