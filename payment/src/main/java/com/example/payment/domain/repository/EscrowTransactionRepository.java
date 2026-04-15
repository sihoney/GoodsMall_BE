package com.example.payment.domain.repository;

import com.example.payment.domain.entity.EscrowTransaction;
import java.util.List;
import java.util.UUID;

public interface EscrowTransactionRepository {

    EscrowTransaction save(EscrowTransaction escrowTransaction);

    List<EscrowTransaction> saveAll(List<EscrowTransaction> escrowTransactions);

    List<EscrowTransaction> findAllByOrderIdOrderByOccurredAtAsc(UUID orderId);

    List<EscrowTransaction> findAllByEscrowIdOrderByOccurredAtAsc(UUID escrowId);
}

