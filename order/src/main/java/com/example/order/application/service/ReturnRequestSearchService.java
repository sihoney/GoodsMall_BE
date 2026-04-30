package com.example.order.application.service;

import com.example.order.application.usecase.ReturnRequestSearchUseCase;
import com.example.order.domain.enumtype.ReturnRequestStatus;
import com.example.order.domain.repository.ReturnRequestRepository;
import com.example.order.presentation.dto.response.ReturnRequestSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReturnRequestSearchService implements ReturnRequestSearchUseCase {

    private final ReturnRequestRepository returnRequestRepository;

    @Override
    public Page<ReturnRequestSummaryResponse> findForSeller(
            UUID sellerMemberId,
            ReturnRequestStatus status,
            Pageable pageable
    ) {
        ReturnRequestStatus targetStatus = status != null ? status : ReturnRequestStatus.RECEIVED;
        return returnRequestRepository
                .findBySellerIdAndStatus(sellerMemberId, targetStatus, pageable)
                .map(ReturnRequestSummaryResponse::from);
    }
}
