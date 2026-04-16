package com.example.member.presentation.controller;

import com.example.member.application.usecase.SellerUsecase;
import com.example.member.presentation.dto.AccountVerificationSendResponse;
import com.example.member.presentation.dto.ApiResponse;
import com.example.member.presentation.dto.SellerRegisterRequest;
import com.example.member.presentation.dto.SellerResponse;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sellers")
@RequiredArgsConstructor
@Tag(name = "Seller", description = "판매자 등록 및 조회 API")
public class SellerController {

    private final SellerUsecase sellerUsecase;

    @PostMapping("/register")
    @Operation(summary = "판매자 등록 요청", description = "계좌 인증을 위한 판매자 등록 요청을 생성합니다.")
    public ResponseEntity<ApiResponse<AccountVerificationSendResponse>> registerSeller(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @RequestBody SellerRegisterRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        sellerUsecase.registerSeller(authenticatedMember.memberId(), request)
                ));
    }

    @GetMapping("/me")
    @Operation(summary = "현재 판매자 정보 조회", description = "현재 회원의 판매자 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<SellerResponse>> getCurrentSeller(
            @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerUsecase.getCurrentSeller(authenticatedMember.memberId())
        ));
    }
}