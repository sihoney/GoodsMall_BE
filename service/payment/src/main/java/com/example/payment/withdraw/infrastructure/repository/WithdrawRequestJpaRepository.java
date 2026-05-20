package com.example.payment.withdraw.infrastructure.repository;

import com.example.payment.withdraw.domain.entity.WithdrawRequest;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawRequestJpaRepository extends JpaRepository<WithdrawRequest, UUID> {

    Page<WithdrawRequest> findByMemberId(UUID memberId, Pageable pageable);
}
