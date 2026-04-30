package com.example.order.presentation.controller;

import com.example.order.application.usecase.ReturnInspectUseCase;
import com.example.order.application.usecase.ReturnRequestSearchUseCase;
import com.example.order.domain.enumtype.ReturnRequestStatus;
import com.example.order.presentation.dto.request.ReturnInspectRequest;
import com.example.order.presentation.dto.response.ApiResponse;
import com.example.order.presentation.dto.response.ReturnInspectResponse;
import com.example.order.presentation.dto.response.ReturnRequestSummaryResponse;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/return-requests")
@RequiredArgsConstructor
public class ReturnRequestController {

    private final ReturnInspectUseCase returnInspectUseCase;
    private final ReturnRequestSearchUseCase returnRequestSearchUseCase;

    @GetMapping("/seller")
    public ResponseEntity<ApiResponse<Page<ReturnRequestSummaryResponse>>> findForSeller(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @RequestParam(required = false) ReturnRequestStatus status,
            @ParameterObject Pageable pageable
    ) {
        UUID sellerMemberId = authenticatedMember.memberId();
        Page<ReturnRequestSummaryResponse> response =
                returnRequestSearchUseCase.findForSeller(sellerMemberId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{returnRequestId}/inspect")
    public ResponseEntity<ApiResponse<ReturnInspectResponse>> inspect(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable UUID returnRequestId,
            @Valid @RequestBody ReturnInspectRequest request
    ) {
        UUID sellerMemberId = authenticatedMember.memberId();
        ReturnInspectResponse response = returnInspectUseCase.inspect(returnRequestId, sellerMemberId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
