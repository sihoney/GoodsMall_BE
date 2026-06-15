package com.example.payment.wallet.infrastructure.repository;

import com.example.payment.wallet.domain.entity.WithdrawRequest;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawRequestJpaRepository extends JpaRepository<WithdrawRequest, UUID> {

    Page<WithdrawRequest> findByMemberId(UUID memberId, Pageable pageable);
}
