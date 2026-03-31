package com.example.member.presentation.dto;

// 회원 프로필 이미지 업로드를 위한 사전 요청 URL을 생성하는 API의 응답 DTO
// : S3 객체 키, 사전 서명된 PUT URL, URL의 만료 시간(초 단위)을 포함하여 클라이언트에 반환
public record ProfileImagePresignResponse(
        String objectKey,
        String uploadUrl,
        long expiresIn
) {
}
