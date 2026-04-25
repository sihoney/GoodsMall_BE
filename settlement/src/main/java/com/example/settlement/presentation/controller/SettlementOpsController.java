package com.example.settlement.presentation.controller;

import com.example.settlement.application.usecase.SettlementPayoutUseCase;
import com.example.settlement.common.exception.CustomException;
import com.example.settlement.common.exception.ErrorCode;
import com.example.settlement.presentation.dto.request.FailedPayoutReplayRequest;
import com.example.settlement.presentation.dto.request.ManualFailedPayoutRequest;
import com.example.settlement.presentation.dto.response.ApiResponse;
import com.example.settlement.presentation.dto.response.FailedPayoutReplayResponse;
import com.example.settlement.presentation.dto.response.ManualFailedPayoutResponse;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * settlement 운영 조치 API 진입점이다.
 * <p>
 * 보상 트랜잭션 운영 시나리오에서 FAILED 정산건 수동 재지급을 트리거한다.
 */
@RestController
@RequestMapping("/api/settlements")
@Tag(name = "Settlement Ops", description = "정산 운영 조치 API")
public class SettlementOpsController {

    private static final int MAX_REPLAY_BATCH_SIZE = 100;

    private final SettlementPayoutUseCase settlementPayoutService;

    public SettlementOpsController(SettlementPayoutUseCase settlementPayoutService) {
        this.settlementPayoutService = settlementPayoutService;
    }

    /**
     * FAILED 정산건 수동 재지급을 요청한다.
     * <p>
     * - 200: 수동 재지급 요청 성공
     * - 400: settlementId 형식 오류/필수값 누락
     * - 404: 대상 settlement 미존재
     * - 409: 현재 상태/실패 사유 정책상 수동 재지급 불가
     */
    @PostMapping("/failed-payout/manual-retry")
    @Operation(summary = "FAILED 정산건 수동 재지급 요청")
    public ResponseEntity<ApiResponse<?>> requestManualFailedPayout(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @RequestBody ManualFailedPayoutRequest request
    ) {
        String rawSettlementId = request == null ? null : request.settlementId();
        if (rawSettlementId == null || rawSettlementId.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(ErrorCode.INVALID_INPUT_VALUE, "settlementId is required."));
        }

        final UUID settlementId;
        try {
            settlementId = UUID.fromString(rawSettlementId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(ErrorCode.INVALID_INPUT_VALUE, "settlementId must be UUID format."));
        }

        try {
            boolean requested = settlementPayoutService.requestManualFailedPayout(settlementId);
            if (!requested) {
                return ResponseEntity.status(ErrorCode.MANUAL_RETRY_NOT_ALLOWED.getHttpStatus())
                        .body(ApiResponse.fail(ErrorCode.MANUAL_RETRY_NOT_ALLOWED));
            }

            return ResponseEntity.ok(ApiResponse.success(ManualFailedPayoutResponse.requested(settlementId)));
        } catch (CustomException exception) {
            return ResponseEntity.status(exception.getErrorCode().getHttpStatus())
                    .body(ApiResponse.fail(
                            exception.getErrorCode(),
                            Objects.requireNonNullElse(exception.getMessage(), exception.getErrorCode().getMessage())
                    ));
        }
    }

    /**
     * DLQ replay 대상 settlementId 목록을 재처리한다.
     * <p>
     * - 200: replay 실행 성공(자동 재요청/수동조치 대상 집계 반환)
     * - 400: settlementIds 누락/비어있음/UUID 형식 오류/허용 배치 초과
     */
    @PostMapping("/failed-payout/replay")
    @Operation(summary = "FAILED 정산건 DLQ replay 처리")
    public ResponseEntity<ApiResponse<?>> replayFailedPayouts(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @RequestBody FailedPayoutReplayRequest request
    ) {
        List<String> rawIds = request == null ? null : request.settlementIds();
        if (rawIds == null || rawIds.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(ErrorCode.INVALID_INPUT_VALUE, "settlementIds is required."));
        }
        if (rawIds.size() > MAX_REPLAY_BATCH_SIZE) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "settlementIds size must be less than or equal to " + MAX_REPLAY_BATCH_SIZE + "."
            ));
        }

        LinkedHashSet<UUID> uniqueSettlementIds = new LinkedHashSet<>();
        for (String rawId : rawIds) {
            if (rawId == null || rawId.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.fail(ErrorCode.INVALID_INPUT_VALUE, "settlementIds must not contain blank values."));
            }
            try {
                uniqueSettlementIds.add(UUID.fromString(rawId));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.fail(ErrorCode.INVALID_INPUT_VALUE, "settlementIds must be UUID format."));
            }
        }

        var result = settlementPayoutService.replayFailedPayouts(new ArrayList<>(uniqueSettlementIds));
        return ResponseEntity.ok(ApiResponse.success(FailedPayoutReplayResponse.from(result)));
    }
}
