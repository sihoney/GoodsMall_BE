package com.example.payment.domain.repository;

import com.example.payment.domain.entity.WithdrawRequest;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WithdrawRequestRepository {

    WithdrawRequest save(WithdrawRequest withdrawRequest);

    Page<WithdrawRequest> findByMemberId(UUID memberId, Pageable pageable);
}
