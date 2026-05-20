package com.example.member.presentation.web;

import com.example.member.application.dto.command.ProfileImagePresignCommand;
import com.example.member.application.service.ProfileImageService;
import com.example.member.presentation.web.dto.ApiResponse;
import com.example.member.presentation.web.dto.ProfileImagePresignRequest;
import com.example.member.presentation.web.dto.ProfileImagePresignResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/profile-images")
@RequiredArgsConstructor
@Tag(name = "프로필 이미지", description = "프로필 이미지 API")
public class ProfileImageController {

    private final ProfileImageService profileImageService;

    @PostMapping("/presign")
    @Operation(summary = "프로필 이미지 업로드 URL 생성", description = "프로필 이미지 업로드용 presigned URL을 생성합니다.")
    public ResponseEntity<ApiResponse<ProfileImagePresignResponse>> createPresignedUploadUrl(
            @Valid @RequestBody ProfileImagePresignRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                ProfileImagePresignResponse.from(
                        profileImageService.createPresignedUpload(
                                new ProfileImagePresignCommand(request.fileName(), request.contentType())
                        )
                )
        ));
    }
}

