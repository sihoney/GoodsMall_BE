package com.example.payment.infrastructure.repository;

import com.example.payment.domain.entity.EscrowTransaction;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EscrowTransactionJpaRepository extends JpaRepository<EscrowTransaction, UUID> {

    List<EscrowTransaction> findAllByOrderIdOrderByOccurredAtAsc(UUID orderId);

    List<EscrowTransaction> findAllByOrderIdAndSellerMemberIdOrderByOccurredAtAsc(UUID orderId, UUID sellerMemberId);

    List<EscrowTransaction> findAllByEscrowIdOrderByOccurredAtAsc(UUID escrowId);
}
