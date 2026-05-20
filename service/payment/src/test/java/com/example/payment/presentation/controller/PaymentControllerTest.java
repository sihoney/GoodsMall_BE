package com.example.payment.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.example.payment.application.dto.ChargeCreateResult;
import com.example.payment.application.dto.WalletSummaryResult;
import com.example.payment.application.usecase.ChargeConfirmUseCase;
import com.example.payment.application.usecase.ChargeCreateUseCase;
import com.example.payment.application.usecase.OrderPaymentApiUseCase;
import com.example.payment.application.usecase.PaymentSearchUseCase;
import com.example.payment.domain.enumtype.ChargeStatus;
import com.example.payment.presentation.dto.request.ChargeCreateRequest;
import com.example.payment.presentation.dto.request.OrderPaymentApiOrderLineRequest;
import com.example.payment.presentation.dto.request.OrderPaymentApiRequest;
import com.example.payment.presentation.dto.response.ApiResponse;
import com.example.payment.presentation.dto.response.ChargeCreateResponse;
import com.example.payment.presentation.dto.response.OrderPaymentApiResponse;
import com.example.payment.presentation.dto.response.WalletSummaryResponse;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
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
class PaymentControllerTest {

    @Mock
    private ChargeCreateUseCase chargeCreateUseCase;

    @Mock
    private ChargeConfirmUseCase chargeConfirmUseCase;

    @Mock
    private PaymentSearchUseCase paymentSearchUseCase;

    @Mock
    private OrderPaymentApiUseCase orderPaymentApiUseCase;

    @InjectMocks
    private PaymentController paymentController;

    @DisplayName("충전 생성 성공 시 201과 공통 응답 래퍼를 반환한다")
    @Test
    void createChargeReturnsCreatedApiResponse() {
        UUID memberId = UUID.randomUUID();
        AuthenticatedMember authenticatedMember = new AuthenticatedMember(memberId, MemberRole.USER, UUID.randomUUID());
        UUID chargeId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();

        given(chargeCreateUseCase.createCharge(org.mockito.ArgumentMatchers.any()))
                .willReturn(new ChargeCreateResult(
                        chargeId,
                        walletId,
                        "pg-order-001",
                        BigDecimal.valueOf(1000L),
                        ChargeStatus.PENDING
                ));

        ResponseEntity<ApiResponse<ChargeCreateResponse>> response = paymentController.createCharge(
                authenticatedMember,
                new ChargeCreateRequest(BigDecimal.valueOf(1000L))
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isNotNull();
        assertThat(response.getBody().data().chargeId()).isEqualTo(chargeId);
        assertThat(response.getBody().data().walletId()).isEqualTo(walletId);
        assertThat(response.getBody().data().amount()).isEqualTo(BigDecimal.valueOf(1000L));
        assertThat(response.getBody().error()).isNull();
    }

    @DisplayName("지갑 요약 조회 성공 시 200과 공통 응답 래퍼를 반환한다")
    @Test
    void findWalletSummaryReturnsOkApiResponse() {
        UUID memberId = UUID.randomUUID();
        AuthenticatedMember authenticatedMember = new AuthenticatedMember(memberId, MemberRole.USER,UUID.randomUUID());
        UUID walletId = UUID.randomUUID();

        given(paymentSearchUseCase.findWalletSummary(memberId)).willReturn(new WalletSummaryResult(
                walletId,
                memberId,
                BigDecimal.valueOf(5000L),
                LocalDateTime.of(2026, 3, 27, 10, 0)
        ));

        ResponseEntity<ApiResponse<WalletSummaryResponse>> response = paymentController.findWalletSummary(authenticatedMember);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isNotNull();
        assertThat(response.getBody().data().walletId()).isEqualTo(walletId);
        assertThat(response.getBody().data().memberId()).isEqualTo(memberId);
        assertThat(response.getBody().data().balance()).isEqualTo(BigDecimal.valueOf(5000L));
        assertThat(response.getBody().error()).isNull();
    }

    @DisplayName("주문 결제 요청 성공 시 200과 공통 응답 래퍼를 반환한다")
    @Test
    void payOrderReturnsOkApiResponse() {
        UUID orderId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        OrderPaymentApiRequest request = new OrderPaymentApiRequest(
                orderId,
                buyerId,
                BigDecimal.valueOf(12000L),
                Instant.parse("2026-04-01T10:00:00Z"),
                List.of(new OrderPaymentApiOrderLineRequest(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        BigDecimal.valueOf(12000L),
                        1,
                        BigDecimal.valueOf(12000L)
                ))
        );
        given(orderPaymentApiUseCase.payOrder(request)).willReturn(new OrderPaymentApiResponse(
                orderId,
                buyerId,
                BigDecimal.valueOf(12000L),
                "SUCCESS",
                null
        ));

        ResponseEntity<ApiResponse<OrderPaymentApiResponse>> response = paymentController.payOrder(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isNotNull();
        assertThat(response.getBody().data().orderId()).isEqualTo(orderId);
        assertThat(response.getBody().data().status()).isEqualTo("SUCCESS");
        assertThat(response.getBody().error()).isNull();
    }
}
