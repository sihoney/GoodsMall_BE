package com.example.order.application.usecase;

import com.example.order.domain.enumtype.ReturnRequestStatus;
import com.example.order.presentation.dto.response.ReturnRequestSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReturnRequestSearchUseCase {

    Page<ReturnRequestSummaryResponse> findForSeller(UUID sellerMemberId, ReturnRequestStatus status, Pageable pageable);
}
