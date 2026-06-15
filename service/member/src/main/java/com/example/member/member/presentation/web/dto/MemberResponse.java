package com.example.member.member.presentation.web.dto;

import com.example.member.member.application.dto.result.MemberResult;
import com.example.member.member.domain.enumtype.MemberStatus;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "회원 프로필 응답")
public record MemberResponse(
        @Schema(description = "회원 ID")
        UUID memberId,
        @Schema(description = "이메일", example = "member@example.com")
        String email,
        @Schema(description = "닉네임", example = "goods-user")
        String nickname,
        @Schema(description = "전화번호", example = "010-1234-5678")
        String phone,
        @Schema(description = "주소", example = "서울")
        String address,
        @Schema(description = "프로필 이미지 URL")
        String profileImageUrl,
        @Schema(description = "회원 역할")
        MemberRole role,
        @Schema(description = "회원 상태")
        MemberStatus status,
        @Schema(description = "생성 시각")
        LocalDateTime createdAt,
        @Schema(description = "수정 시각")
        LocalDateTime updatedAt
) {

    public static MemberResponse from(MemberResult result) {
        return new MemberResponse(
                result.memberId(),
                result.email(),
                result.nickname(),
                result.phone(),
                result.address(),
                result.profileImageUrl(),
                result.role(),
                result.status(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
