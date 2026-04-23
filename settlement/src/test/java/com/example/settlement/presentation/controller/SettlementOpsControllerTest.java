package com.example.settlement.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.settlement.application.dto.FailedPayoutReplayResult;
import com.example.settlement.application.usecase.SettlementPayoutUseCase;
import com.example.settlement.presentation.dto.request.FailedPayoutReplayRequest;
import com.example.settlement.presentation.dto.request.ManualFailedPayoutRequest;
import com.example.settlement.presentation.dto.response.ApiResponse;
import com.example.settlement.presentation.dto.response.FailedPayoutReplayResponse;
import com.example.settlement.presentation.dto.response.ManualFailedPayoutResponse;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementOpsController 테스트")
class SettlementOpsControllerTest {

    private static final AuthenticatedMember AUTHENTICATED_MEMBER =
            new AuthenticatedMember(UUID.randomUUID(), MemberRole.ADMIN, UUID.randomUUID());

    @Mock
    private SettlementPayoutUseCase settlementPayoutService;

    @InjectMocks
    private SettlementOpsController settlementOpsController;

    @Test
    @DisplayName("수동 재지급이 허용되면 200 응답을 반환한다")
    void requestManualFailedPayout_whenAllowed_returnsOk() {
        UUID settlementId = UUID.randomUUID();
        when(settlementPayoutService.requestManualFailedPayout(settlementId)).thenReturn(true);

        ResponseEntity<ApiResponse<?>> response = settlementOpsController.requestManualFailedPayout(
                AUTHENTICATED_MEMBER,
                new ManualFailedPayoutRequest(settlementId.toString())
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isInstanceOf(ManualFailedPayoutResponse.class);
        verify(settlementPayoutService).requestManualFailedPayout(settlementId);
    }

    @Test
    @DisplayName("settlementId 형식이 잘못되면 400 응답을 반환한다")
    void requestManualFailedPayout_whenInvalidUuid_returnsBadRequest() {
        ResponseEntity<ApiResponse<?>> response = settlementOpsController.requestManualFailedPayout(
                AUTHENTICATED_MEMBER,
                new ManualFailedPayoutRequest("invalid-uuid")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().error()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("INVALID_INPUT_VALUE");
    }

    @Test
    @DisplayName("대상 settlement가 없으면 404 응답을 반환한다")
    void requestManualFailedPayout_whenSettlementNotFound_returnsNotFound() {
        UUID settlementId = UUID.randomUUID();
        when(settlementPayoutService.requestManualFailedPayout(settlementId))
                .thenThrow(new IllegalArgumentException("Settlement not found: " + settlementId));

        ResponseEntity<ApiResponse<?>> response = settlementOpsController.requestManualFailedPayout(
                AUTHENTICATED_MEMBER,
                new ManualFailedPayoutRequest(settlementId.toString())
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().error()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("SETTLEMENT_NOT_FOUND");
    }

    @Test
    @DisplayName("수동 재지급이 정책상 불가하면 409 응답을 반환한다")
    void requestManualFailedPayout_whenNotAllowed_returnsConflict() {
        UUID settlementId = UUID.randomUUID();
        when(settlementPayoutService.requestManualFailedPayout(settlementId)).thenReturn(false);

        ResponseEntity<ApiResponse<?>> response = settlementOpsController.requestManualFailedPayout(
                AUTHENTICATED_MEMBER,
                new ManualFailedPayoutRequest(settlementId.toString())
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().error()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("MANUAL_RETRY_NOT_ALLOWED");
        verify(settlementPayoutService).requestManualFailedPayout(settlementId);
    }

    @Test
    @DisplayName("DLQ replay 요청이 유효하면 200 응답과 집계 결과를 반환한다")
    void replayFailedPayouts_whenValidRequest_returnsOk() {
        UUID settlementId1 = UUID.randomUUID();
        UUID settlementId2 = UUID.randomUUID();
        when(settlementPayoutService.replayFailedPayouts(List.of(settlementId1, settlementId2)))
                .thenReturn(new FailedPayoutReplayResult(1, 1, 0, 0));

        ResponseEntity<ApiResponse<?>> response = settlementOpsController.replayFailedPayouts(
                AUTHENTICATED_MEMBER,
                new FailedPayoutReplayRequest(List.of(settlementId1.toString(), settlementId2.toString()))
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isInstanceOf(FailedPayoutReplayResponse.class);
    }

    @Test
    @DisplayName("DLQ replay 요청 목록이 비어 있으면 400 응답을 반환한다")
    void replayFailedPayouts_whenEmptyRequest_returnsBadRequest() {
        ResponseEntity<ApiResponse<?>> response = settlementOpsController.replayFailedPayouts(
                AUTHENTICATED_MEMBER,
                new FailedPayoutReplayRequest(List.of())
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().error()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("INVALID_INPUT_VALUE");
    }

    @Test
    @DisplayName("DLQ replay 요청 목록에 UUID 형식이 아니면 400 응답을 반환한다")
    void replayFailedPayouts_whenInvalidUuid_returnsBadRequest() {
        ResponseEntity<ApiResponse<?>> response = settlementOpsController.replayFailedPayouts(
                AUTHENTICATED_MEMBER,
                new FailedPayoutReplayRequest(List.of("invalid-uuid"))
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().error()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("INVALID_INPUT_VALUE");
    }

    @Test
    @DisplayName("DLQ replay 요청 목록에 중복 settlementId가 있으면 중복 제거 후 서비스로 전달한다")
    void replayFailedPayouts_whenDuplicateIds_deduplicatesBeforeServiceCall() {
        UUID settlementId1 = UUID.randomUUID();
        UUID settlementId2 = UUID.randomUUID();
        when(settlementPayoutService.replayFailedPayouts(List.of(settlementId1, settlementId2)))
                .thenReturn(new FailedPayoutReplayResult(1, 0, 0, 0));

        ResponseEntity<ApiResponse<?>> response = settlementOpsController.replayFailedPayouts(
                AUTHENTICATED_MEMBER,
                new FailedPayoutReplayRequest(List.of(
                        settlementId1.toString(),
                        settlementId2.toString(),
                        settlementId1.toString()
                ))
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(settlementPayoutService).replayFailedPayouts(List.of(settlementId1, settlementId2));
    }

    @Test
    @DisplayName("DLQ replay 요청 목록이 허용 배치 크기를 초과하면 400 응답을 반환한다")
    void replayFailedPayouts_whenExceedsMaxBatchSize_returnsBadRequest() {
        java.util.ArrayList<String> largeIds = new java.util.ArrayList<>();
        for (int index = 0; index < 101; index++) {
            largeIds.add(UUID.randomUUID().toString());
        }

        ResponseEntity<ApiResponse<?>> response = settlementOpsController.replayFailedPayouts(
                AUTHENTICATED_MEMBER,
                new FailedPayoutReplayRequest(largeIds)
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().error()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("INVALID_INPUT_VALUE");
    }
}
