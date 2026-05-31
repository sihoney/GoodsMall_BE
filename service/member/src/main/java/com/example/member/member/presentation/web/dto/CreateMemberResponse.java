package com.example.member.member.presentation.web.dto;

import com.example.member.member.application.dto.result.CreateMemberResult;
import com.example.member.member.domain.enumtype.MemberStatus;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "회원 가입 응답")
public record CreateMemberResponse(
        @Schema(description = "회원 ID")
        UUID memberId,
        @Schema(description = "닉네임", example = "goods-user")
        String nickname,
        @Schema(description = "프로필 이미지 URL")
        String profileImageUrl,
        @Schema(description = "회원 역할")
        MemberRole role,
        @Schema(description = "회원 상태")
        MemberStatus status,
        @Schema(description = "생성 시각")
        LocalDateTime createdAt
) {

    public static CreateMemberResponse from(CreateMemberResult result) {
        return new CreateMemberResponse(
                result.memberId(),
                result.nickname(),
                result.profileImageUrl(),
                result.role(),
                result.status(),
                result.createdAt()
        );
    }
}
