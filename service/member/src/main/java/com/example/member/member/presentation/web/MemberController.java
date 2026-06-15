package com.example.member.member.presentation.web;

import com.example.member.auth.application.dto.command.ChangePasswordCommand;
import com.example.member.auth.presentation.web.dto.ChangePasswordRequest;
import com.example.member.auth.presentation.web.dto.ChangePasswordResponse;
import com.example.member.common.presentation.web.dto.ApiResponse;
import com.example.member.member.application.dto.command.CreateMemberCommand;
import com.example.member.member.application.dto.command.UpdateMemberCommand;
import com.example.member.member.application.dto.command.WithdrawMemberCommand;
import com.example.member.member.application.dto.query.GetMemberQuery;
import com.example.member.member.application.port.in.MemberUsecase;
import com.example.member.member.presentation.web.dto.CreateMemberRequest;
import com.example.member.member.presentation.web.dto.CreateMemberResponse;
import com.example.member.member.presentation.web.dto.MemberResponse;
import com.example.member.member.presentation.web.dto.UpdateMemberRequest;
import com.example.member.member.presentation.web.dto.WithdrawMemberRequest;
import com.example.member.member.presentation.web.dto.WithdrawMemberResponse;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "회원", description = "가입/프로필")
public class MemberController {

    private final MemberUsecase memberUsecase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "회원 가입", description = "회원 생성")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "생성"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "중복")
    })
    public ResponseEntity<ApiResponse<CreateMemberResponse>> createMember(
            @Valid @RequestBody CreateMemberRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                CreateMemberResponse.from(memberUsecase.createMember(new CreateMemberCommand(
                        request.email(),
                        request.password(),
                        request.nickname(),
                        request.phone(),
                        request.address(),
                        request.profileImageKey(),
                        MemberRole.USER
                )))
        ));
    }

    @GetMapping("/{memberId}")
    @Operation(summary = "회원 조회", description = "회원 프로필")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원 없음")
    })
    public MemberResponse getMember(
        @Parameter(description = "회원 ID", example = "11111111-1111-1111-1111-111111111111")
        @PathVariable(name = "memberId") UUID memberId
    ) {
        return MemberResponse.from(memberUsecase.getMember(new GetMemberQuery(memberId)));
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "프로필 조회")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원 없음")
    })
    public ResponseEntity<ApiResponse<MemberResponse>> getCurrentMember(
            @Parameter(hidden = true) @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                MemberResponse.from(memberUsecase.getCurrentMember(
                        new GetMemberQuery(authenticatedMember.memberId())
                ))
        ));
    }

    @PatchMapping("/me")
    @Operation(summary = "내 정보 수정", description = "프로필 수정")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    public ResponseEntity<ApiResponse<MemberResponse>> updateCurrentMember(
            @Parameter(hidden = true) @CurrentMember AuthenticatedMember authenticatedMember,
            @Valid @RequestBody UpdateMemberRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                MemberResponse.from(memberUsecase.updateCurrentMember(new UpdateMemberCommand(
                        authenticatedMember.memberId(),
                        request.nickname(),
                        request.phone(),
                        request.address(),
                        request.profileImageKey()
                )))
        ));
    }

    @PatchMapping("/me/password")
    @Operation(summary = "비밀번호 변경", description = "내 비밀번호")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    public ResponseEntity<ApiResponse<ChangePasswordResponse>> changeCurrentMemberPassword(
            @Parameter(hidden = true) @CurrentMember AuthenticatedMember authenticatedMember,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(new ChangePasswordResponse(
                memberUsecase.changeCurrentMemberPassword(new ChangePasswordCommand(
                        authenticatedMember.memberId(),
                        request.currentPassword(),
                        request.newPassword()
                )).message()
        )));
    }

    @DeleteMapping("/me")
    @Operation(summary = "회원 탈퇴", description = "내 계정")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    public ResponseEntity<ApiResponse<WithdrawMemberResponse>> withdrawCurrentMember(
            @Parameter(hidden = true) @CurrentMember AuthenticatedMember authenticatedMember,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @Valid @RequestBody WithdrawMemberRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                WithdrawMemberResponse.from(memberUsecase.withdrawCurrentMember(new WithdrawMemberCommand(
                        authenticatedMember.memberId(),
                        request.currentPassword(),
                        authorizationHeader
                )))
        ));
    }

}
