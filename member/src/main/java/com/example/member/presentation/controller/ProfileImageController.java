package com.example.member.presentation.controller;

import com.example.member.application.service.ProfileImageService;
import com.example.member.presentation.dto.ApiResponse;
import com.example.member.presentation.dto.ProfileImagePresignRequest;
import com.example.member.presentation.dto.ProfileImagePresignResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members/profile-images")
@RequiredArgsConstructor
@Tag(name = "회원 프로필 이미지", description = "회원 프로필 이미지 API")
public class ProfileImageController {

    private final ProfileImageService profileImageService;

    @PostMapping("/presign")
    @Operation(summary = "프로필 이미지 업로드 사전 요청", description = "회원 프로필 이미지 업로드를 위한 사전 요청 URL을 생성합니다.")
    public ResponseEntity<ApiResponse<ProfileImagePresignResponse>> createPresignedUploadUrl(
            @RequestBody ProfileImagePresignRequest request // fileName과 contentType을 포함하는 요청 DTO
    ) {
        return ResponseEntity.ok(ApiResponse.success(profileImageService.createPresignedUpload(request)));
    }
}
