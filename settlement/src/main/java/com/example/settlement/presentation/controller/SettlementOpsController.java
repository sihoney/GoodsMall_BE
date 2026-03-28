package com.example.settlement.presentation.controller;

import com.example.settlement.application.service.SettlementPayoutService;
import com.example.settlement.common.exception.ErrorCode;
import com.example.settlement.presentation.dto.request.ManualFailedPayoutRequest;
import com.example.settlement.presentation.dto.response.ApiResponse;
import com.example.settlement.presentation.dto.response.ManualFailedPayoutResponse;
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
@RequestMapping("/api/settlement/ops")
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
}
