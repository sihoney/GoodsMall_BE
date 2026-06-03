package com.example.member.member.presentation.web.dto;

import com.todaylunch.common.security.auth.enumtype.MemberRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "회원 가입 요청")
public record CreateMemberRequest(
        @NotBlank
        @Email
        @Schema(description = "이메일", example = "member@example.com")
        String email,
        @NotBlank
        @Size(min = 8, max = 100)
        @Schema(description = "비밀번호", example = "password1234")
        String password,
        @NotBlank
        @Size(max = 30)
        @Schema(description = "닉네임", example = "goods-user")
        String nickname,
        @Schema(description = "전화번호", example = "010-1234-5678")
        String phone,
        @Schema(description = "주소", example = "서울")
        String address,
        @Schema(description = "프로필 이미지 키", example = "members/profile/11111111/avatar.png")
        String profileImageKey,
        @Schema(description = "회원 역할")
        MemberRole role
) {
}
