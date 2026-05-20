package com.example.payment.payment.application.service;

import com.example.payment.wallet.application.dto.ChargeDetailResult;
import com.example.payment.wallet.application.dto.ChargeListItemResult;
import com.example.payment.escrow.application.dto.EscrowTransactionItemResult;
import com.example.payment.payment.application.dto.OrderPaymentDetailResult;
import com.example.payment.common.application.dto.PagedResult;
import com.example.payment.wallet.application.dto.PendingSellerIncomeItemResult;
import com.example.payment.wallet.application.dto.WalletSummaryResult;
import com.example.payment.wallet.application.dto.WalletTransactionItemResult;
import com.example.payment.wallet.application.dto.WithdrawListItemResult;
import com.example.payment.payment.application.usecase.PaymentSearchUseCase;
import com.example.payment.common.exception.ChargeNotFoundException;
import com.example.payment.common.exception.OrderPaymentNotFoundException;
import com.example.payment.common.exception.WalletNotFoundException;
import com.example.payment.wallet.domain.entity.Charge;
import com.example.payment.escrow.domain.entity.Escrow;
import com.example.payment.escrow.domain.entity.EscrowTransaction;
import com.example.payment.payment.domain.entity.OrderPayment;
import com.example.payment.wallet.domain.entity.Wallet;
import com.example.payment.wallet.domain.entity.WalletTransaction;
import com.example.payment.wallet.domain.entity.WithdrawRequest;
import com.example.payment.wallet.domain.repository.ChargeRepository;
import com.example.payment.escrow.domain.repository.EscrowRepository;
import com.example.payment.escrow.domain.repository.EscrowTransactionRepository;
import com.example.payment.payment.domain.repository.OrderPaymentRepository;
import com.example.payment.wallet.domain.repository.WalletRepository;
import com.example.payment.wallet.domain.repository.WalletTransactionRepository;
import com.example.payment.wallet.domain.repository.WithdrawRequestRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ?꾨줎??留덉씠?섏씠吏?먯꽌 ?꾩슂??payment 議고쉶 ?좎뒪耳?댁뒪瑜??대떦?쒕떎.
 * 紐⑤뱺 議고쉶???몄쬆 而⑦뀓?ㅽ듃??memberId 湲곗??쇰줈 蹂몄씤 ?곗씠?곕쭔 諛섑솚?쒕떎.
 */
@Service
@Transactional(readOnly = true)
public class PaymentSearchService implements PaymentSearchUseCase {

    private static final int MAX_PAGE_SIZE = 100;

    private final WalletRepository walletRepository;
    private final ChargeRepository chargeRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final EscrowRepository escrowRepository;
    private final EscrowTransactionRepository escrowTransactionRepository;
    private final WithdrawRequestRepository withdrawRequestRepository;
    private final OrderPaymentRepository orderPaymentRepository;

    public PaymentSearchService(
            WalletRepository walletRepository,
            ChargeRepository chargeRepository,
            WalletTransactionRepository walletTransactionRepository,
            EscrowRepository escrowRepository,
            EscrowTransactionRepository escrowTransactionRepository,
            WithdrawRequestRepository withdrawRequestRepository,
            OrderPaymentRepository orderPaymentRepository
    ) {
        this.walletRepository = walletRepository;
        this.chargeRepository = chargeRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.escrowRepository = escrowRepository;
        this.escrowTransactionRepository = escrowTransactionRepository;
        this.withdrawRequestRepository = withdrawRequestRepository;
        this.orderPaymentRepository = orderPaymentRepository;
    }

    @Override
    public WalletSummaryResult findWalletSummary(UUID memberId) {
        Wallet wallet = findWallet(memberId);
        return new WalletSummaryResult(
                wallet.getWalletId(),
                wallet.getMemberId(),
                wallet.getBalance(),
                wallet.getUpdatedAt()
        );
    }

    @Override
    public PagedResult<ChargeListItemResult> findAllCharges(UUID memberId, int page, int size) {
        Page<Charge> chargePage = chargeRepository.findByMemberId(
                memberId,
                createPageRequest(page, size, "requestedAt")
        );

        return toPagedResult(chargePage.map(this::toChargeListItemResult));
    }

    @Override
    public ChargeDetailResult findChargeDetail(UUID memberId, UUID chargeId) {
        Charge charge = chargeRepository.findByChargeIdAndMemberId(chargeId, memberId)
                .orElseThrow(ChargeNotFoundException::new);

        return new ChargeDetailResult(
                charge.getChargeId(),
                charge.getMemberId(),
                charge.getWalletId(),
                charge.getRequestedAmount(),
                charge.getApprovedAmount(),
                charge.getTossBankCode(),
                charge.getPgOrderId(),
                charge.getPgPaymentKey(),
                charge.getChargeStatus(),
                charge.getRequestedAt(),
                charge.getApprovedAt(),
                charge.getFailedAt(),
                charge.getFailureReason()
        );
    }

    @Override
    public PagedResult<WalletTransactionItemResult> findAllTransactions(UUID memberId, int page, int size) {
        Wallet wallet = findWallet(memberId);
        Page<WalletTransaction> transactionPage = walletTransactionRepository.findByWalletId(
                wallet.getWalletId(),
                createPageRequest(page, size, "createdAt")
        );
        return toPagedResult(transactionPage.map(this::toWalletTransactionItemResult));
    }

    @Override
    public PagedResult<PendingSellerIncomeItemResult> findAllPendingSellerIncomes(UUID memberId, int page, int size) {
        Page<Escrow> escrowPage = escrowRepository.findPendingBySellerMemberId(
                memberId,
                createPageRequest(page, size, "createdAt")
        );

        return toPagedResult(escrowPage.map(this::toPendingSellerIncomeItemResult));
    }

    @Override
    public PagedResult<WithdrawListItemResult> findAllWithdrawRequests(UUID memberId, int page, int size) {
        Page<WithdrawRequest> withdrawRequestPage = withdrawRequestRepository.findByMemberId(
                memberId,
                createPageRequest(page, size, "requestedAt")
        );

        return toPagedResult(withdrawRequestPage.map(this::toWithdrawListItemResult));
    }

    @Override
    public OrderPaymentDetailResult findOrderPaymentByOrderId(UUID memberId, UUID orderId) {
        OrderPayment orderPayment = orderPaymentRepository.findByOrderIdAndBuyerMemberId(orderId, memberId)
                .orElseThrow(OrderPaymentNotFoundException::new);

        return new OrderPaymentDetailResult(
                orderPayment.getOrderPaymentId(),
                orderPayment.getOrderId(),
                orderPayment.getTotalAmount(),
                orderPayment.getPaymentMethod(),
                orderPayment.getPaymentStatus(),
                orderPayment.getPaidAt()
        );
    }

    @Override
    public List<EscrowTransactionItemResult> findEscrowTransactionsByOrderId(UUID sellerMemberId, UUID orderId) {
        return escrowTransactionRepository.findAllByOrderIdAndSellerMemberIdOrderByOccurredAtAsc(orderId, sellerMemberId)
                .stream()
                .map(this::toEscrowTransactionItemResult)
                .toList();
    }

    private Wallet findWallet(UUID memberId) {
        return walletRepository.findByMemberId(memberId)
                .orElseThrow(WalletNotFoundException::new);
    }

    private Pageable createPageRequest(int page, int size, String sortBy) {
        if (page < 0) {
            throw new IllegalArgumentException("?섏씠吏 踰덊샇??0 ?댁긽?댁뼱???⑸땲??");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("?섏씠吏 ?ш린??1 ?댁긽?댁뼱???⑸땲??");
        }
        if (size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("?섏씠吏 ?ш린??100??珥덇낵?????놁뒿?덈떎.");
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
    }

    private ChargeListItemResult toChargeListItemResult(Charge charge) {
        return new ChargeListItemResult(
                charge.getChargeId(),
                charge.getRequestedAmount(),
                charge.getApprovedAmount(),
                charge.getChargeStatus(),
                charge.getTossBankCode(),
                charge.getRequestedAt(),
                charge.getApprovedAt(),
                charge.getFailedAt()
        );
    }

    private WalletTransactionItemResult toWalletTransactionItemResult(WalletTransaction walletTransaction) {
        return new WalletTransactionItemResult(
                walletTransaction.getTransactionId(),
                walletTransaction.getTransactionType(),
                walletTransaction.getAmount(),
                walletTransaction.getBalanceAfter(),
                walletTransaction.getReferenceType(),
                walletTransaction.getReferenceId(),
                walletTransaction.getDescription(),
                walletTransaction.getCreatedAt()
        );
    }

    private PendingSellerIncomeItemResult toPendingSellerIncomeItemResult(Escrow escrow) {
        return new PendingSellerIncomeItemResult(
                escrow.getEscrowId(),
                escrow.getOrderId(),
                escrow.getAmount(),
                escrow.getEscrowStatus(),
                escrow.getCreatedAt(),
                escrow.getUpdatedAt()
        );
    }

    private EscrowTransactionItemResult toEscrowTransactionItemResult(EscrowTransaction escrowTransaction) {
        return new EscrowTransactionItemResult(
                escrowTransaction.getEscrowTransactionId(),
                escrowTransaction.getEscrowId(),
                escrowTransaction.getOrderId(),
                escrowTransaction.getOrderItemId(),
                escrowTransaction.getSellerMemberId(),
                escrowTransaction.getBuyerMemberId(),
                escrowTransaction.getTransactionType(),
                escrowTransaction.getAmount(),
                escrowTransaction.getBeforeAmount(),
                escrowTransaction.getAfterAmount(),
                escrowTransaction.getReferenceId(),
                escrowTransaction.getReferenceType(),
                escrowTransaction.getDescription(),
                escrowTransaction.getOccurredAt(),
                escrowTransaction.getCreatedAt()
        );
    }

    private WithdrawListItemResult toWithdrawListItemResult(WithdrawRequest withdrawRequest) {
        return new WithdrawListItemResult(
                withdrawRequest.getWithdrawRequestId(),
                withdrawRequest.getAmount(),
                withdrawRequest.getFee(),
                withdrawRequest.getActualAmount(),
                withdrawRequest.getMaskedBankAccount(),
                withdrawRequest.getStatus(),
                withdrawRequest.getRequestedAt(),
                withdrawRequest.getProcessedAt()
        );
    }

    private <T> PagedResult<T> toPagedResult(Page<T> page) {
        return new PagedResult<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
