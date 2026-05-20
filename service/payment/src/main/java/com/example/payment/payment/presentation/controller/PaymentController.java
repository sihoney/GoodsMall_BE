package com.example.payment.payment.presentation.controller;

import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import com.example.payment.card.application.dto.CardPaymentConfirmCommand;
import com.example.payment.charge.application.dto.ChargeConfirmCommand;
import com.example.payment.charge.application.dto.ChargeConfirmFailureCommand;
import com.example.payment.charge.application.dto.ChargeCreateCommand;
import com.example.payment.auction.application.dto.AuctionDepositCommand;
import com.example.payment.refund.application.dto.PaymentRefundCommand;
import com.example.payment.refund.application.dto.PaymentRefundItemCommand;
import com.example.payment.refund.application.dto.SellerRefundCommand;
import com.example.payment.withdraw.application.dto.WithdrawCommand;
import com.example.payment.auction.application.usecase.AuctionDepositUseCase;
import com.example.payment.card.application.usecase.CardPaymentConfirmUseCase;
import com.example.payment.charge.application.usecase.ChargeConfirmFailureUseCase;
import com.example.payment.charge.application.usecase.ChargeConfirmUseCase;
import com.example.payment.charge.application.usecase.ChargeCreateUseCase;
import com.example.payment.orderpayment.application.usecase.OrderPaymentApiUseCase;
import com.example.payment.refund.application.usecase.PaymentCancellationUseCase;
import com.example.payment.payment.application.usecase.PaymentSearchUseCase;
import com.example.payment.refund.application.usecase.SellerRefundUseCase;
import com.example.payment.withdraw.application.usecase.WithdrawUseCase;
import com.example.payment.card.presentation.dto.request.CardPaymentConfirmRequest;
import com.example.payment.charge.presentation.dto.request.ChargeConfirmFailureRequest;
import com.example.payment.charge.presentation.dto.request.ChargeConfirmRequest;
import com.example.payment.charge.presentation.dto.request.ChargeCreateRequest;
import com.example.payment.auction.presentation.dto.request.AuctionFeeVerificationRequest;
import com.example.payment.orderpayment.presentation.dto.request.OrderPaymentApiRequest;
import com.example.payment.refund.presentation.dto.request.PaymentCancellationRequest;
import com.example.payment.refund.presentation.dto.request.SellerRefundConfirmRequest;
import com.example.payment.withdraw.presentation.dto.request.WithdrawCreateRequest;
import com.example.payment.common.presentation.dto.response.ApiResponse;
import com.example.payment.auction.presentation.dto.response.AuctionFeeVerificationResponse;
import com.example.payment.card.presentation.dto.response.CardPaymentConfirmResponse;
import com.example.payment.charge.presentation.dto.response.ChargeConfirmFailureResponse;
import com.example.payment.charge.presentation.dto.response.ChargeConfirmResponse;
import com.example.payment.charge.presentation.dto.response.ChargeCreateResponse;
import com.example.payment.charge.presentation.dto.response.ChargeDetailResponse;
import com.example.payment.charge.presentation.dto.response.ChargeListItemResponse;
import com.example.payment.escrow.presentation.dto.response.EscrowTransactionItemResponse;
import com.example.payment.orderpayment.presentation.dto.response.OrderPaymentApiResponse;
import com.example.payment.orderpayment.presentation.dto.response.OrderPaymentDetailResponse;
import com.example.payment.common.presentation.dto.response.PagedResponse;
import com.example.payment.refund.presentation.dto.response.PaymentRefundResponse;
import com.example.payment.settlement.presentation.dto.response.PendingSellerIncomeItemResponse;
import com.example.payment.wallet.presentation.dto.response.WalletSummaryResponse;
import com.example.payment.wallet.presentation.dto.response.WalletTransactionItemResponse;
import com.example.payment.withdraw.presentation.dto.response.WithdrawListItemResponse;
import com.example.payment.withdraw.presentation.dto.response.WithdrawResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment", description = "異⑹쟾/吏媛??섎텋 API")
public class PaymentController {

    private final AuctionDepositUseCase auctionDepositUseCase;
    private final ChargeCreateUseCase chargeCreateUseCase;
    private final ChargeConfirmUseCase chargeConfirmUseCase;
    private final CardPaymentConfirmUseCase cardPaymentConfirmUseCase;
    private final ChargeConfirmFailureUseCase chargeConfirmFailureUseCase;
    private final PaymentCancellationUseCase paymentCancellationUseCase;
    private final SellerRefundUseCase sellerRefundUseCase;
    private final PaymentSearchUseCase paymentSearchUseCase;
    private final OrderPaymentApiUseCase orderPaymentApiUseCase;
    private final WithdrawUseCase withdrawUseCase;

    public PaymentController(
            AuctionDepositUseCase auctionDepositUseCase,
            ChargeCreateUseCase chargeCreateUseCase,
            ChargeConfirmUseCase chargeConfirmUseCase,
            CardPaymentConfirmUseCase cardPaymentConfirmUseCase,
            ChargeConfirmFailureUseCase chargeConfirmFailureUseCase,
            PaymentCancellationUseCase paymentCancellationUseCase,
            SellerRefundUseCase sellerRefundUseCase,
            PaymentSearchUseCase paymentSearchUseCase,
            OrderPaymentApiUseCase orderPaymentApiUseCase,
            WithdrawUseCase withdrawUseCase
    ) {
        this.auctionDepositUseCase = auctionDepositUseCase;
        this.chargeCreateUseCase = chargeCreateUseCase;
        this.chargeConfirmUseCase = chargeConfirmUseCase;
        this.cardPaymentConfirmUseCase = cardPaymentConfirmUseCase;
        this.chargeConfirmFailureUseCase = chargeConfirmFailureUseCase;
        this.paymentCancellationUseCase = paymentCancellationUseCase;
        this.sellerRefundUseCase = sellerRefundUseCase;
        this.paymentSearchUseCase = paymentSearchUseCase;
        this.orderPaymentApiUseCase = orderPaymentApiUseCase;
        this.withdrawUseCase = withdrawUseCase;
    }

    @PostMapping("/auctions/bid-fees")
    @Operation(summary = "寃쎈ℓ ?덉튂湲?李④컧 諛??섎텋 泥섎━")
    public ResponseEntity<ApiResponse<AuctionFeeVerificationResponse>> verifyAuctionDeposit(
            @Valid @RequestBody AuctionFeeVerificationRequest request
    ) {
        AuctionFeeVerificationResponse response = AuctionFeeVerificationResponse.success(
                auctionDepositUseCase.processAuctionDeposit(new AuctionDepositCommand(
                        request.bidId(),
                        request.auctionId(),
                        request.highestBidderId(),
                        request.highestBidderFee()
                ))
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/wallet")
    @Operation(summary = "???덉튂湲?湲덉븸 議고쉶")
    public ResponseEntity<ApiResponse<WalletSummaryResponse>> findWalletSummary(
            @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        WalletSummaryResponse response = WalletSummaryResponse.from(
                paymentSearchUseCase.findWalletSummary(authenticatedMember.memberId())
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/charges")
    @Operation(summary = "??異⑹쟾 紐⑸줉 議고쉶")
    public ResponseEntity<ApiResponse<PagedResponse<ChargeListItemResponse>>> findAllCharges(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var result = paymentSearchUseCase.findAllCharges(authenticatedMember.memberId(), page, size);
        List<ChargeListItemResponse> items = result.items()
                                                   .stream()
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

    @GetMapping("/charges/{chargeId}")
    @Operation(summary = "異⑹쟾 ?곸꽭 議고쉶")
    public ResponseEntity<ApiResponse<ChargeDetailResponse>> findChargeDetail(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable UUID chargeId
    ) {
        ChargeDetailResponse response = ChargeDetailResponse.from(
                paymentSearchUseCase.findChargeDetail(authenticatedMember.memberId(), chargeId)
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/transactions")
    @Operation(summary = "吏媛?嫄곕옒?댁뿭 議고쉶")
    public ResponseEntity<ApiResponse<PagedResponse<WalletTransactionItemResponse>>> findAllTransactions(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var result = paymentSearchUseCase.findAllTransactions(authenticatedMember.memberId(), page, size);
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

    @GetMapping("/seller/pending-incomes")
    @Operation(summary = "?먮ℓ??吏湲??湲??댁뿭 議고쉶")
    public ResponseEntity<ApiResponse<PagedResponse<PendingSellerIncomeItemResponse>>> findAllPendingSellerIncomes(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var result = paymentSearchUseCase.findAllPendingSellerIncomes(authenticatedMember.memberId(), page, size);
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

    @GetMapping("/withdrawals")
    @Operation(summary = "?덉튂湲?異쒓툑 ?댁뿭 議고쉶")
    public ResponseEntity<ApiResponse<PagedResponse<WithdrawListItemResponse>>> findAllWithdrawRequests(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var result = paymentSearchUseCase.findAllWithdrawRequests(authenticatedMember.memberId(), page, size);
        List<WithdrawListItemResponse> items = result.items().stream()
                                                     .map(WithdrawListItemResponse::from)
                                                     .toList();
        PagedResponse<WithdrawListItemResponse> response = new PagedResponse<>(
                items,
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/seller/orders/{orderId}/escrow-transactions")
    @Operation(summary = "?먮ℓ??二쇰Ц escrow transaction ?대젰 議고쉶")
    public ResponseEntity<ApiResponse<List<EscrowTransactionItemResponse>>> findSellerOrderEscrowTransactions(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable UUID orderId
    ) {
        if (authenticatedMember.role() != MemberRole.SELLER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "?먮ℓ??沅뚰븳???꾩슂?⑸땲??");
        }

        List<EscrowTransactionItemResponse> response = paymentSearchUseCase.findEscrowTransactionsByOrderId(
                                                                                   authenticatedMember.memberId(),
                                                                                   orderId
                                                                           ).stream()
                                                                           .map(EscrowTransactionItemResponse::from)
                                                                           .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/charge")
    @Operation(summary = "異⑹쟾 ?붿껌 ?앹꽦")
    public ResponseEntity<ApiResponse<ChargeCreateResponse>> createCharge(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @Valid @RequestBody ChargeCreateRequest request
    ) {
        ChargeCreateCommand command = new ChargeCreateCommand(
                authenticatedMember.memberId(),
                request.amount()
        );
        // response ??蹂?섏씠 而⑦듃濡ㅻ윭?먯꽌 ?대（?댁???寃껋? ?꾩돺吏留??대┛?꾪궎?띿쿂??application 寃곌낵瑜??몃? ?묐떟 ?뺤떇?쇰줈
        // 諛붽씀??寃껋? ?몃? 怨꾩링??梨낆엫?대뒗 ai???먮떒???곸젅?섎떎怨??앷컖??
        ChargeCreateResponse response = ChargeCreateResponse.from(chargeCreateUseCase.createCharge(command));
        // 肄붾뵫 而⑤깽?섏뿉???뺥븳 怨듯넻 ?묐떟?쇰줈 諛섑솚
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/charge/fail")
    @Operation(summary = "異⑹쟾 ?ㅽ뙣 諛섏쁺")
    public ResponseEntity<ApiResponse<ChargeConfirmFailureResponse>> confirmChargeFailure(
            @Valid @RequestBody ChargeConfirmFailureRequest request
    ) {
        ChargeConfirmFailureCommand command = new ChargeConfirmFailureCommand(
                request.orderId(),
                request.code(),
                request.message()
        );
        ChargeConfirmFailureResponse response = ChargeConfirmFailureResponse.from(
                chargeConfirmFailureUseCase.confirmChargeFailure(command)
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/confirm")
    @Operation(summary = "異⑹쟾 ?뱀씤 ?뺤젙")
    public ResponseEntity<ApiResponse<ChargeConfirmResponse>> confirmCharge(
            @Valid @RequestBody ChargeConfirmRequest request
    ) {
        ChargeConfirmCommand command = new ChargeConfirmCommand(
                request.chargeId(),
                request.paymentKey(),
                request.orderId(),
                request.amount()
        );
        ChargeConfirmResponse response = ChargeConfirmResponse.from(chargeConfirmUseCase.confirmCharge(command));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/card/confirm")
    @Operation(summary = "移대뱶 寃곗젣 ?뱀씤 ?뺤젙")
    public ResponseEntity<ApiResponse<CardPaymentConfirmResponse>> confirmCardPayment(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @Valid @RequestBody CardPaymentConfirmRequest request
    ) {
        CardPaymentConfirmCommand command = new CardPaymentConfirmCommand(
                authenticatedMember.memberId(),
                request.orderId(),
                request.paymentKey(),
                request.amount()
        );
        CardPaymentConfirmResponse response = CardPaymentConfirmResponse.from(
                cardPaymentConfirmUseCase.confirmCardPayment(command)
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/cancellations")
    @Operation(summary = "二쇰Ц 痍⑥냼 ?붿껌")
    public ResponseEntity<ApiResponse<PaymentRefundResponse>> requestOrderCancellation(
            @Valid @RequestBody PaymentCancellationRequest request
    ) {
        PaymentRefundCommand command = new PaymentRefundCommand(
                request.orderId(),
                request.buyerMemberId(),
                request.orderCancelRequestId(),
                request.refundType(),
                request.reason(),
                request.items().stream()
                       .map(item -> new PaymentRefundItemCommand(item.orderItemId(), item.refundAmount()))
                       .toList()
        );
        PaymentRefundResponse response = PaymentRefundResponse.from(
                paymentCancellationUseCase.requestCancellation(command));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/seller/refunds/confirm")
    @Operation(summary = "?먮ℓ??諛섑뭹 ?섎졊 ?뺤씤 ???섎텋")
    public ResponseEntity<ApiResponse<PaymentRefundResponse>> requestSellerRefundConfirm(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @Valid @RequestBody SellerRefundConfirmRequest request
    ) {
        if (authenticatedMember.role() != MemberRole.SELLER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "?먮ℓ??沅뚰븳???꾩슂?⑸땲??");
        }

        SellerRefundCommand command = new SellerRefundCommand(
                request.orderId(),
                authenticatedMember.memberId(),
                request.orderCancelRequestId(),
                request.refundType(),
                request.reason(),
                request.items().stream()
                       .map(item -> item.orderItemId())
                       .toList()
        );
        PaymentRefundResponse response = PaymentRefundResponse.from(sellerRefundUseCase.requestSellerRefund(command));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/orders/{orderId}")
    @Operation(summary = "二쇰Ц 寃곗젣 ?뺣낫 議고쉶")
    public ResponseEntity<ApiResponse<OrderPaymentDetailResponse>> findOrderPayment(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable UUID orderId
    ) {
        OrderPaymentDetailResponse response = OrderPaymentDetailResponse.from(
                paymentSearchUseCase.findOrderPaymentByOrderId(authenticatedMember.memberId(), orderId)
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/orders")
    @Operation(summary = "二쇰Ц 寃곗젣")
    public ResponseEntity<ApiResponse<OrderPaymentApiResponse>> payOrder(
            @Valid @RequestBody OrderPaymentApiRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(orderPaymentApiUseCase.payOrder(request)));
    }

    @PostMapping("/withdrawals")
    @Operation(summary = "?덉튂湲?異쒓툑 ?붿껌")
    public ResponseEntity<ApiResponse<WithdrawResponse>> withdraw(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @Valid @RequestBody WithdrawCreateRequest request
    ) {
        WithdrawResponse response = WithdrawResponse.from(withdrawUseCase.withdraw(
                new WithdrawCommand(
                        authenticatedMember.memberId(),
                        request.amount(),
                        request.bankAccount(),
                        request.accountHolder()
                )
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}
