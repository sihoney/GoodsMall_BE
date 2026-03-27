package com.example.payment.presentation.controller;

import com.example.payment.application.dto.ChargeConfirmCommand;
import com.example.payment.application.dto.ChargeCreateCommand;
import com.example.payment.application.dto.ChargeRefundCommand;
import com.example.payment.application.usecase.ChargeConfirmUseCase;
import com.example.payment.application.usecase.ChargeCreateUseCase;
import com.example.payment.application.usecase.ChargeRefundUseCase;
import com.example.payment.application.usecase.PaymentSearchUseCase;
import com.example.payment.domain.enumtype.PgProvider;
import com.example.payment.presentation.dto.request.ChargeConfirmRequest;
import com.example.payment.presentation.dto.request.ChargeCreateRequest;
import com.example.payment.presentation.dto.request.ChargeRefundRequest;
import com.example.payment.presentation.dto.response.ApiResponse;
import com.example.payment.presentation.dto.response.ChargeDetailResponse;
import com.example.payment.presentation.dto.response.ChargeConfirmResponse;
import com.example.payment.presentation.dto.response.ChargeCreateResponse;
import com.example.payment.presentation.dto.response.ChargeListItemResponse;
import com.example.payment.presentation.dto.response.ChargeRefundSummaryResponse;
import com.example.payment.presentation.dto.response.ChargeRefundResponse;
import com.example.payment.presentation.dto.response.PagedResponse;
import com.example.payment.presentation.dto.response.PendingSellerIncomeItemResponse;
import com.example.payment.presentation.dto.response.WalletSummaryResponse;
import com.example.payment.presentation.dto.response.WalletTransactionItemResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
/**
 * payment 충전 API 진입점이다.
 * HTTP 요청을 application command로 변환하고, usecase 결과를 presentation 응답 DTO로 매핑한다.
 */
public class PaymentController {

    private final ChargeCreateUseCase chargeCreateUseCase;
    private final ChargeConfirmUseCase chargeConfirmUseCase;
    private final ChargeRefundUseCase chargeRefundUseCase;
    private final PaymentSearchUseCase paymentSearchUseCase;

    public PaymentController(
            ChargeCreateUseCase chargeCreateUseCase,
            ChargeConfirmUseCase chargeConfirmUseCase,
            ChargeRefundUseCase chargeRefundUseCase,
            PaymentSearchUseCase paymentSearchUseCase
    ) {
        this.chargeCreateUseCase = chargeCreateUseCase;
        this.chargeConfirmUseCase = chargeConfirmUseCase;
        this.chargeRefundUseCase = chargeRefundUseCase;
        this.paymentSearchUseCase = paymentSearchUseCase;
    }

    /**
     * 네비게이션과 마이페이지에서 공통으로 사용하는 wallet 요약을 반환한다.
     */
    @GetMapping("/wallet")
    public ResponseEntity<ApiResponse<WalletSummaryResponse>> findWalletSummary(@RequestHeader("X-Member-Id") UUID memberId) {
        WalletSummaryResponse response = WalletSummaryResponse.from(paymentSearchUseCase.findWalletSummary(memberId));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 회원의 charge 목록을 최신순 페이지 응답으로 반환한다.
     */
    @GetMapping("/charges")
    public ResponseEntity<ApiResponse<PagedResponse<ChargeListItemResponse>>> findAllCharges(
            @RequestHeader("X-Member-Id") UUID memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var result = paymentSearchUseCase.findAllCharges(memberId, page, size);
        List<ChargeListItemResponse> items = result.items().stream()
                .map(ChargeListItemResponse::from)
                .toList();
        PagedResponse<ChargeListItemResponse> response = new PagedResponse<>(
                items,
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 단건 charge 상세와 최신 refund 이력을 함께 반환한다.
     */
    @GetMapping("/charges/{chargeId}")
    public ResponseEntity<ApiResponse<ChargeDetailResponse>> findChargeDetail(
            @RequestHeader("X-Member-Id") UUID memberId,
            @PathVariable UUID chargeId
    ) {
        ChargeDetailResponse response = ChargeDetailResponse.from(paymentSearchUseCase.findChargeDetail(memberId, chargeId));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 회원의 charge refund 목록을 최신순 페이지 응답으로 반환한다.
     */
    @GetMapping("/refunds")
    public ResponseEntity<ApiResponse<PagedResponse<ChargeRefundSummaryResponse>>> findAllRefunds(
            @RequestHeader("X-Member-Id") UUID memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var result = paymentSearchUseCase.findAllRefunds(memberId, page, size);
        List<ChargeRefundSummaryResponse> items = result.items().stream()
                .map(ChargeRefundSummaryResponse::from)
                .toList();
        PagedResponse<ChargeRefundSummaryResponse> response = new PagedResponse<>(
                items,
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * wallet 거래 내역을 프론트 표시용 페이지 응답으로 반환한다.
     */
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<PagedResponse<WalletTransactionItemResponse>>> findAllTransactions(
            @RequestHeader("X-Member-Id") UUID memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var result = paymentSearchUseCase.findAllTransactions(memberId, page, size);
        List<WalletTransactionItemResponse> items = result.items().stream()
                .map(WalletTransactionItemResponse::from)
                .toList();
        PagedResponse<WalletTransactionItemResponse> response = new PagedResponse<>(
                items,
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 판매자 기준으로 아직 wallet에 반영되지 않은 HELD escrow를 반환한다.
     */
    @GetMapping("/seller/pending-incomes")
    public ResponseEntity<ApiResponse<PagedResponse<PendingSellerIncomeItemResponse>>> findAllPendingSellerIncomes(
            @RequestHeader("X-Member-Id") UUID memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var result = paymentSearchUseCase.findAllPendingSellerIncomes(memberId, page, size);
        List<PendingSellerIncomeItemResponse> items = result.items().stream()
                .map(PendingSellerIncomeItemResponse::from)
                .toList();
        PagedResponse<PendingSellerIncomeItemResponse> response = new PagedResponse<>(
                items,
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 충전 요청을 생성하고 PG 승인에 필요한 charge 식별 정보를 반환한다.
     */
    @PostMapping("/charge")
    public ResponseEntity<ApiResponse<ChargeCreateResponse>> createCharge(
            @RequestHeader("X-Member-Id") UUID memberId,
            @Valid @RequestBody ChargeCreateRequest request
    ) {
        ChargeCreateCommand command = new ChargeCreateCommand(memberId, request.amount(), PgProvider.TOSS);
        ChargeCreateResponse response = ChargeCreateResponse.from(chargeCreateUseCase.createCharge(command));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * PG 승인 결과를 받아 charge와 wallet 상태를 확정한다.
     */
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<ChargeConfirmResponse>> confirmCharge(@Valid @RequestBody ChargeConfirmRequest request) {
        ChargeConfirmCommand command = new ChargeConfirmCommand(
                request.chargeId(),
                request.paymentKey(),
                request.orderId(),
                request.amount()
        );
        ChargeConfirmResponse response = ChargeConfirmResponse.from(chargeConfirmUseCase.confirmCharge(command));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 승인된 charge를 환불하고 wallet 잔액을 차감한다.
     */
    @PostMapping("/charges/{chargeId}/refund")
    public ResponseEntity<ApiResponse<ChargeRefundResponse>> refundCharge(
            @PathVariable UUID chargeId,
            @Valid @RequestBody ChargeRefundRequest request
    ) {
        ChargeRefundCommand command = new ChargeRefundCommand(chargeId, request.refundReason());
        ChargeRefundResponse response = ChargeRefundResponse.from(chargeRefundUseCase.refundCharge(command));
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
