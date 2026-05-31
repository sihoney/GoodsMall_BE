package com.example.member.seller.presentation.web;

import com.example.member.common.presentation.web.dto.ApiResponse;
import com.example.member.seller.application.dto.command.SellerRegisterCommand;
import com.example.member.seller.application.port.in.SellerUsecase;
import com.example.member.seller.presentation.web.dto.SellerRegisterRequest;
import com.example.member.seller.presentation.web.dto.SellerResponse;
import com.example.member.verification.presentation.web.dto.AccountVerificationSendResponse;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "판매자", description = "등록/프로필")
public class SellerController {

    private final SellerUsecase sellerUsecase;

    @PostMapping("/register")
    @Operation(summary = "판매자 등록", description = "계좌 인증")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "생성"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "중복/진행 중")
    })
    public ResponseEntity<ApiResponse<AccountVerificationSendResponse>> registerSeller(
            @Parameter(hidden = true) @CurrentMember AuthenticatedMember authenticatedMember,
            @Valid @RequestBody SellerRegisterRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        AccountVerificationSendResponse.from(
                                sellerUsecase.registerSeller(
                                        authenticatedMember.memberId(),
                                        new SellerRegisterCommand(request.bankName(), request.account())
                                )
                        )
                ));
    }

    @GetMapping("/me")
    @Operation(summary = "내 판매자 조회", description = "판매자 프로필")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "판매자 없음")
    })
    public ResponseEntity<ApiResponse<SellerResponse>> getCurrentSeller(
            @Parameter(hidden = true) @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                SellerResponse.from(sellerUsecase.getCurrentSeller(authenticatedMember.memberId()))
        ));
    }
}
