package com.example.member.member.presentation.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "회원 수정 요청")
public record UpdateMemberRequest(
        @NotBlank
        @Size(max = 30)
        @Schema(description = "닉네임", example = "updated-user")
        String nickname,
        @Schema(description = "전화번호", example = "010-9876-5432")
        String phone,
        @Schema(description = "주소", example = "부산")
        String address,
        @Schema(description = "프로필 이미지 키", example = "members/profile/11111111/avatar.png")
        String profileImageKey
) {
}
