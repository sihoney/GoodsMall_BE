package com.example.payment.application.service;

import com.example.payment.application.dto.ChargeDetailResult;
import com.example.payment.application.dto.ChargeListItemResult;
import com.example.payment.application.dto.ChargeRefundSummaryResult;
import com.example.payment.application.dto.PagedResult;
import com.example.payment.application.dto.PendingSellerIncomeItemResult;
import com.example.payment.application.dto.WalletSummaryResult;
import com.example.payment.application.dto.WalletTransactionItemResult;
import com.example.payment.application.usecase.PaymentQueryUseCase;
import com.example.payment.common.exception.ChargeNotFoundException;
import com.example.payment.common.exception.WalletNotFoundException;
import com.example.payment.domain.entity.Charge;
import com.example.payment.domain.entity.ChargeRefund;
import com.example.payment.domain.entity.Escrow;
import com.example.payment.domain.entity.Wallet;
import com.example.payment.domain.entity.WalletTransaction;
import com.example.payment.domain.repository.ChargeRefundRepository;
import com.example.payment.domain.repository.ChargeRepository;
import com.example.payment.domain.repository.EscrowRepository;
import com.example.payment.domain.repository.WalletRepository;
import com.example.payment.domain.repository.WalletTransactionRepository;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
/**
 * 프론트 마이페이지에서 필요한 payment 조회를 담당한다.
 * 모든 조회는 gateway가 전달한 memberId 기준으로 자기 데이터만 읽도록 제한한다.
 */
public class PaymentQueryService implements PaymentQueryUseCase {

    private static final int MAX_PAGE_SIZE = 100;

    private final WalletRepository walletRepository;
    private final ChargeRepository chargeRepository;
    private final ChargeRefundRepository chargeRefundRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final EscrowRepository escrowRepository;

    public PaymentQueryService(
            WalletRepository walletRepository,
            ChargeRepository chargeRepository,
            ChargeRefundRepository chargeRefundRepository,
            WalletTransactionRepository walletTransactionRepository,
            EscrowRepository escrowRepository
    ) {
        this.walletRepository = walletRepository;
        this.chargeRepository = chargeRepository;
        this.chargeRefundRepository = chargeRefundRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.escrowRepository = escrowRepository;
    }

    @Override
    public WalletSummaryResult getWalletSummary(UUID memberId) {
        Wallet wallet = findWallet(memberId);
        return new WalletSummaryResult(
                wallet.getWalletId(),
                wallet.getMemberId(),
                wallet.getBalance(),
                wallet.getUpdatedAt()
        );
    }

    @Override
    public PagedResult<ChargeListItemResult> getCharges(UUID memberId, int page, int size) {
        Page<Charge> chargePage = chargeRepository.findByMemberId(
                memberId,
                createPageRequest(page, size, "requestedAt")
        );

        return toPagedResult(chargePage.map(this::toChargeListItemResult));
    }

    @Override
    public ChargeDetailResult getChargeDetail(UUID memberId, UUID chargeId) {
        Charge charge = chargeRepository.findByChargeIdAndMemberId(chargeId, memberId)
                .orElseThrow(ChargeNotFoundException::new);

        ChargeRefundSummaryResult latestRefund = chargeRefundRepository.findTopByChargeIdOrderByRequestedAtDesc(chargeId)
                .map(this::toChargeRefundSummaryResult)
                .orElse(null);

        return new ChargeDetailResult(
                charge.getChargeId(),
                charge.getMemberId(),
                charge.getWalletId(),
                charge.getRequestedAmount(),
                charge.getApprovedAmount(),
                charge.getPgProvider(),
                charge.getPgOrderId(),
                charge.getPgPaymentKey(),
                charge.getChargeStatus(),
                charge.getRequestedAt(),
                charge.getApprovedAt(),
                charge.getFailedAt(),
                charge.getFailureReason(),
                latestRefund != null,
                latestRefund
        );
    }

    @Override
    public PagedResult<ChargeRefundSummaryResult> getRefunds(UUID memberId, int page, int size) {
        Page<ChargeRefund> refundPage = chargeRefundRepository.findByMemberId(
                memberId,
                createPageRequest(page, size, "requestedAt")
        );

        return toPagedResult(refundPage.map(this::toChargeRefundSummaryResult));
    }

    @Override
    public PagedResult<WalletTransactionItemResult> getTransactions(UUID memberId, int page, int size) {
        Wallet wallet = findWallet(memberId);
        Page<WalletTransaction> transactionPage = walletTransactionRepository.findByWalletId(
                wallet.getWalletId(),
                createPageRequest(page, size, "createdAt")
        );

        return toPagedResult(transactionPage.map(this::toWalletTransactionItemResult));
    }

    @Override
    public PagedResult<PendingSellerIncomeItemResult> getPendingSellerIncomes(UUID memberId, int page, int size) {
        Page<Escrow> escrowPage = escrowRepository.findPendingBySellerMemberId(
                memberId,
                createPageRequest(page, size, "createdAt")
        );

        return toPagedResult(escrowPage.map(this::toPendingSellerIncomeItemResult));
    }

    private Wallet findWallet(UUID memberId) {
        return walletRepository.findByMemberId(memberId)
                .orElseThrow(WalletNotFoundException::new);
    }

    private Pageable createPageRequest(int page, int size, String sortBy) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be zero or positive.");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size must be positive.");
        }
        if (size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size must not exceed 100.");
        }

        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
    }

    private ChargeListItemResult toChargeListItemResult(Charge charge) {
        return new ChargeListItemResult(
                charge.getChargeId(),
                charge.getRequestedAmount(),
                charge.getApprovedAmount(),
                charge.getChargeStatus(),
                charge.getPgProvider(),
                charge.getRequestedAt(),
                charge.getApprovedAt(),
                charge.getFailedAt()
        );
    }

    private ChargeRefundSummaryResult toChargeRefundSummaryResult(ChargeRefund chargeRefund) {
        return new ChargeRefundSummaryResult(
                chargeRefund.getChargeRefundId(),
                chargeRefund.getChargeId(),
                chargeRefund.getRefundAmount(),
                chargeRefund.getRefundStatus(),
                chargeRefund.getRefundReason(),
                chargeRefund.getRequestedAt(),
                chargeRefund.getRefundedAt(),
                chargeRefund.getFailedAt()
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
                escrow.getReleaseAt(),
                escrow.getCreatedAt(),
                escrow.getUpdatedAt()
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
