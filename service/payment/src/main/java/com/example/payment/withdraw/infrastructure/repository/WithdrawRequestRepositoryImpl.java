package com.example.payment.withdraw.infrastructure.repository;

import com.example.payment.withdraw.domain.entity.WithdrawRequest;
import com.example.payment.withdraw.domain.repository.WithdrawRequestRepository;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class WithdrawRequestRepositoryImpl implements WithdrawRequestRepository {

    private final WithdrawRequestJpaRepository withdrawRequestJpaRepository;

    public WithdrawRequestRepositoryImpl(WithdrawRequestJpaRepository withdrawRequestJpaRepository) {
        this.withdrawRequestJpaRepository = withdrawRequestJpaRepository;
    }

    @Override
    public WithdrawRequest save(WithdrawRequest withdrawRequest) {
        return withdrawRequestJpaRepository.save(withdrawRequest);
    }

    @Override
    public Page<WithdrawRequest> findByMemberId(UUID memberId, Pageable pageable) {
        return withdrawRequestJpaRepository.findByMemberId(memberId, pageable);
    }
}
