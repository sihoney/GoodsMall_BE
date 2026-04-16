package com.example.ai.presentation.controller;

import com.example.ai.application.usecase.EmbeddingAdminUseCase;
import com.example.ai.presentation.dto.response.ApiResponse;
import com.example.ai.presentation.dto.response.EmbeddingAdminResponse;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import com.todaylunch.common.security.auth.util.RoleGuard;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "누락 임베딩 추가하기")
    public ResponseEntity<ApiResponse<EmbeddingAdminResponse>> backfillMissing(
            @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        RoleGuard.requireAdmin(authenticatedMember);
        EmbeddingAdminResponse response = EmbeddingAdminResponse.from(embeddingAdminUseCase.backfillMissing());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/reindex-all")
    @Operation(summary = "전체 임베딩 재실행")
    public ResponseEntity<ApiResponse<EmbeddingAdminResponse>> reindexAll(
            @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        RoleGuard.requireAdmin(authenticatedMember);
        EmbeddingAdminResponse response = EmbeddingAdminResponse.from(embeddingAdminUseCase.reindexAll());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

