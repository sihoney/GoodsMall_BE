package com.example.payment.infrastructure.repository;

import com.example.payment.domain.entity.CardTransaction;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardTransactionJpaRepository extends JpaRepository<CardTransaction, UUID> {

    List<CardTransaction> findByTransactionGroupId(UUID transactionGroupId);
}
