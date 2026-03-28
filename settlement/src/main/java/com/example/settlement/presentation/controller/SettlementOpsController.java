package com.example.settlement.presentation.controller;

import com.example.settlement.application.service.SettlementPayoutService;
import com.example.settlement.common.exception.ErrorCode;
import com.example.settlement.presentation.dto.request.FailedPayoutReplayRequest;
import com.example.settlement.presentation.dto.request.ManualFailedPayoutRequest;
import com.example.settlement.presentation.dto.response.ApiResponse;
import com.example.settlement.presentation.dto.response.FailedPayoutReplayResponse;
import com.example.settlement.presentation.dto.response.ManualFailedPayoutResponse;
import java.util.ArrayList;
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
@RequestMapping("/api/settlement")
public class SettlementOpsController {

    private final SettlementPayoutService settlementPayoutService;

    public SettlementOpsController(SettlementPayoutService settlementPayoutService) {
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
    public ResponseEntity<ApiResponse<?>> requestManualFailedPayout(@RequestBody ManualFailedPayoutRequest request) {
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
        } catch (IllegalArgumentException e) {
            String message = Objects.requireNonNullElse(e.getMessage(), "Invalid request.");
            if (message.startsWith("Settlement not found:")) {
                return ResponseEntity.status(ErrorCode.SETTLEMENT_NOT_FOUND.getHttpStatus())
                        .body(ApiResponse.fail(ErrorCode.SETTLEMENT_NOT_FOUND, message));
            }
            return ResponseEntity.badRequest().body(ApiResponse.fail(ErrorCode.INVALID_INPUT_VALUE, message));
        }
    }

    /**
     * DLQ replay 대상 settlementId 목록을 재처리한다.
     * <p>
     * - 200: replay 실행 성공(자동 재요청/수동조치 대상 집계 반환)
     * - 400: settlementIds 누락/비어있음/UUID 형식 오류
     */
    @PostMapping("/failed-payout/replay")
    public ResponseEntity<ApiResponse<?>> replayFailedPayouts(@RequestBody FailedPayoutReplayRequest request) {
        List<String> rawIds = request == null ? null : request.settlementIds();
        if (rawIds == null || rawIds.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(ErrorCode.INVALID_INPUT_VALUE, "settlementIds is required."));
        }

        List<UUID> settlementIds = new ArrayList<>();
        for (String rawId : rawIds) {
            if (rawId == null || rawId.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.fail(ErrorCode.INVALID_INPUT_VALUE, "settlementIds must not contain blank values."));
            }
            try {
                settlementIds.add(UUID.fromString(rawId));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.fail(ErrorCode.INVALID_INPUT_VALUE, "settlementIds must be UUID format."));
            }
        }

        var result = settlementPayoutService.replayFailedPayouts(settlementIds);
        return ResponseEntity.ok(ApiResponse.success(FailedPayoutReplayResponse.from(result)));
    }
}
