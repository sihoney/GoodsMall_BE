package com.example.member.presentation.controller;

import com.example.member.application.service.MemberService;
import com.example.member.presentation.dto.ApiResponse;
import com.example.member.presentation.dto.CreateMemberRequest;
import com.example.member.presentation.dto.MemberResponse;
import com.example.member.presentation.dto.UpdateMemberRequest;
import com.example.member.presentation.resolver.AuthenticatedMember;
import com.example.member.presentation.resolver.CurrentMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Tag(name = "Member", description = "사용자 CRUD API")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary="사용자 생성", description="사용자을 생성합니다.")
    public ResponseEntity<ApiResponse> createMember(
        @Validated @RequestBody CreateMemberRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberService.createMember(request)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponse>> getCurrentMember(
        @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            memberService.getCurrentMember(authenticatedMember.memberId())
        ));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponse>> updateCurrentMember(
        @CurrentMember AuthenticatedMember authenticatedMember,
        @RequestBody UpdateMemberRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            memberService.updateCurrentMember(authenticatedMember.memberId(), request)
        ));
    }

    @GetMapping("/{memberId}")
    @Operation(summary="사용자 단건 조회", description="(개발용) 사용자 단건을 조회합니다.")
    public MemberResponse getMember(@PathVariable UUID memberId) {
        return memberService.getMember(memberId);
    }

    @PutMapping("/{memberId}")
    @Operation(summary="사용자 수정", description="(개발용) 사용자를 수정합니다.")
    public MemberResponse updateMember(
        @PathVariable UUID memberId,
        @RequestBody UpdateMemberRequest request
    ) {
        return memberService.updateMember(memberId, request);
    }
}
