package com.example.ai.presentation.controller;

import com.example.ai.application.usecase.EmbeddingAdminUseCase;
import com.example.ai.presentation.dto.response.ApiResponse;
import com.example.ai.presentation.dto.response.EmbeddingAdminResponse;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import com.todaylunch.common.security.auth.util.RoleGuard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai/admin/embeddings")
@Tag(name = "AI Embedding Admin", description = "AI 임베딩 관리자 API")
public class EmbeddingAdminController {

    private final EmbeddingAdminUseCase embeddingAdminUseCase;

    @PostMapping("/backfill-missing")
    @Operation(summary = "누락 임베딩 추가")
    @Parameter(
            name = "Authorization",
            in = ParameterIn.HEADER,
            required = true,
            description = "Bearer 액세스 토큰",
            example = "Bearer eyJhbGciOiJIUzI1NiJ9..."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "백필 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "processedCount": 120,
                                        "successCount": 80,
                                        "skippedCount": 35,
                                        "failedCount": 5
                                      },
                                      "error": null
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "data": null,
                                      "error": {
                                        "code": "INVALID_TOKEN",
                                        "message": "유효하지 않은 토큰입니다."
                                      }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "관리자 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "data": null,
                                      "error": {
                                        "code": "FORBIDDEN",
                                        "message": "접근 권한이 없습니다."
                                      }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류"
            )
    })
    public ResponseEntity<ApiResponse<EmbeddingAdminResponse>> backfillMissing(
            @Parameter(hidden = true) @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        RoleGuard.requireAdmin(authenticatedMember);
        EmbeddingAdminResponse response = EmbeddingAdminResponse.from(embeddingAdminUseCase.backfillMissing());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/reindex-all")
    @Operation(summary = "전체 임베딩 재색인")
    @Parameter(
            name = "Authorization",
            in = ParameterIn.HEADER,
            required = true,
            description = "Bearer 액세스 토큰",
            example = "Bearer eyJhbGciOiJIUzI1NiJ9..."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "재색인 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "processedCount": 120,
                                        "successCount": 115,
                                        "skippedCount": 0,
                                        "failedCount": 5
                                      },
                                      "error": null
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "관리자 권한 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류"
            )
    })
    public ResponseEntity<ApiResponse<EmbeddingAdminResponse>> reindexAll(
            @Parameter(hidden = true) @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        RoleGuard.requireAdmin(authenticatedMember);
        EmbeddingAdminResponse response = EmbeddingAdminResponse.from(embeddingAdminUseCase.reindexAll());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
