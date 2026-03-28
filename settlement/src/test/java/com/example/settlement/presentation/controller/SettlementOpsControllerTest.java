package com.example.settlement.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.settlement.application.service.SettlementPayoutService;
import com.example.settlement.presentation.dto.request.ManualFailedPayoutRequest;
import com.example.settlement.presentation.dto.response.ApiResponse;
import com.example.settlement.presentation.dto.response.ManualFailedPayoutResponse;
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

    @Mock
    private SettlementPayoutService settlementPayoutService;

    @InjectMocks
    private SettlementOpsController settlementOpsController;

    @Test
    @DisplayName("수동 재지급이 허용되면 200 응답을 반환한다")
    void requestManualFailedPayout_whenAllowed_returnsOk() {
        UUID settlementId = UUID.randomUUID();
        when(settlementPayoutService.requestManualFailedPayout(settlementId)).thenReturn(true);

        ResponseEntity<ApiResponse<?>> response = settlementOpsController.requestManualFailedPayout(
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
                new ManualFailedPayoutRequest(settlementId.toString())
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().error()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("MANUAL_RETRY_NOT_ALLOWED");
        verify(settlementPayoutService).requestManualFailedPayout(settlementId);
    }
}

