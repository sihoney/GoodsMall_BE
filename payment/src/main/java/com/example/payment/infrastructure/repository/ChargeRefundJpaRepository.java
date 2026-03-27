package com.example.payment.infrastructure.repository;

import com.example.payment.domain.entity.ChargeRefund;
import com.example.payment.domain.enumtype.ChargeRefundStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChargeRefundJpaRepository extends JpaRepository<ChargeRefund, UUID> {

    boolean existsByChargeIdAndRefundStatus(UUID chargeId, ChargeRefundStatus refundStatus);

    Optional<ChargeRefund> findTopByChargeIdOrderByRequestedAtDesc(UUID chargeId);

    @Query("""
            select chargeRefund
            from ChargeRefund chargeRefund
            where chargeRefund.chargeId in (
                select charge.chargeId
                from Charge charge
                where charge.memberId = :memberId
            )
            """)
    Page<ChargeRefund> findByMemberId(@Param("memberId") UUID memberId, Pageable pageable);
}
