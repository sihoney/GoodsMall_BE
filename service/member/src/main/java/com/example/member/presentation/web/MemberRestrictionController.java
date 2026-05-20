package com.example.member.presentation.web;

import com.example.member.application.dto.command.CreateMemberRestrictionCommand;
import com.example.member.application.port.in.MemberRestrictionUsecase;
import com.example.member.presentation.web.dto.ApiResponse;
import com.example.member.presentation.web.dto.CreateMemberRestrictionRequest;
import com.example.member.presentation.web.dto.MemberRestrictionResponse;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/member-restrictions")
@RequiredArgsConstructor
@Tag(name = "회원 제재", description = "관리자 회원 제재 API")
public class MemberRestrictionController {

    private final MemberRestrictionUsecase memberRestrictionUsecase;

    @PostMapping
    @Operation(summary = "회원 제재 생성", description = "회원 제재를 생성합니다.")
    public ResponseEntity<ApiResponse<MemberRestrictionResponse>> createRestriction(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @Valid @RequestBody CreateMemberRestrictionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(MemberRestrictionResponse.from(
                        memberRestrictionUsecase.createRestriction(
                                authenticatedMember,
                                new CreateMemberRestrictionCommand(
                                        request.memberId(),
                                        request.reason(),
                                        request.restrictionType(),
                                        request.durationHours()
                                )
                        )
                )));
    }

    @PatchMapping("/{restrictionId}/deactivate")
    @Operation(summary = "회원 제재 비활성화", description = "기존 회원 제재를 비활성화합니다.")
    public ResponseEntity<ApiResponse<MemberRestrictionResponse>> deactivateRestriction(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable(name = "restrictionId") UUID restrictionId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                MemberRestrictionResponse.from(
                        memberRestrictionUsecase.deactivateRestriction(authenticatedMember, restrictionId)
                )
        ));
    }

    @GetMapping
    @Operation(summary = "전체 회원 제재 조회", description = "관리자가 전체 회원의 제재 이력을 조회합니다.")
    public ResponseEntity<ApiResponse<List<MemberRestrictionResponse>>> getAllMemberRestrictions(
            @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                memberRestrictionUsecase.getAllMemberRestrictions(authenticatedMember).stream()
                        .map(MemberRestrictionResponse::from)
                        .toList()
        ));
    }

    @GetMapping("/members/{memberId}")
    @Operation(summary = "회원 제재 조회", description = "특정 회원의 모든 제재를 조회합니다.")
    public ResponseEntity<ApiResponse<List<MemberRestrictionResponse>>> getMemberRestrictions(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable(name = "memberId") UUID memberId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                memberRestrictionUsecase.getMemberRestrictions(authenticatedMember, memberId).stream()
                        .map(MemberRestrictionResponse::from)
                        .toList()
        ));
    }
}


