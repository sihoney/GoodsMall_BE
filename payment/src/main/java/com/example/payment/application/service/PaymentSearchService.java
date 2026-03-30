package com.example.payment.application.service;

import com.example.payment.application.dto.ChargeDetailResult;
import com.example.payment.application.dto.ChargeListItemResult;
import com.example.payment.application.dto.ChargeRefundSummaryResult;
import com.example.payment.application.dto.PagedResult;
import com.example.payment.application.dto.PendingSellerIncomeItemResult;
import com.example.payment.application.dto.WalletSummaryResult;
import com.example.payment.application.dto.WalletTransactionItemResult;
import com.example.payment.application.usecase.PaymentSearchUseCase;
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

/**
 * 프론트 마이페이지에서 필요한 payment 조회 유스케이스를 담당한다.
 * 모든 조회는 인증 컨텍스트의 memberId 기준으로 본인 데이터만 읽도록 제한한다.
 */
@Service
@Transactional(readOnly = true)
public class PaymentSearchService implements PaymentSearchUseCase {

    private static final int MAX_PAGE_SIZE = 100;

    private final WalletRepository walletRepository;
    private final ChargeRepository chargeRepository;
    private final ChargeRefundRepository chargeRefundRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final EscrowRepository escrowRepository;

    public PaymentSearchService(
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

    /**
     * wallet aggregate에서 네비/마이페이지 공통 요약 정보를 꺼낸다.
     */
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

    /**
     * charge 엔티티 목록을 최신 요청 시각 기준으로 페이지 조회한다.
     */
    @Override
    public PagedResult<ChargeListItemResult> findAllCharges(UUID memberId, int page, int size) {
        Page<Charge> chargePage = chargeRepository.findByMemberId(
                memberId,
                createPageRequest(page, size, "requestedAt")
        );

        return toPagedResult(chargePage.map(this::toChargeListItemResult));
    }

    /**
     * 단건 charge를 본인 소유 여부까지 함께 확인해 상세 응답으로 조립한다.
     */
    @Override
    public ChargeDetailResult findChargeDetail(UUID memberId, UUID chargeId) {
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

    /**
     * refund 엔티티를 charge 소유 기준으로 필터링해 목록 응답으로 변환한다.
     */
    @Override
    public PagedResult<ChargeRefundSummaryResult> findAllRefunds(UUID memberId, int page, int size) {
        Page<ChargeRefund> refundPage = chargeRefundRepository.findByMemberId(
                memberId,
                createPageRequest(page, size, "requestedAt")
        );

        return toPagedResult(refundPage.map(this::toChargeRefundSummaryResult));
    }

    /**
     * wallet 거래 이력을 walletId 기준으로 조회해 프론트 표시용 응답으로 변환한다.
     */
    @Override
    public PagedResult<WalletTransactionItemResult> findAllTransactions(UUID memberId, int page, int size) {
        Wallet wallet = findWallet(memberId);
        Page<WalletTransaction> transactionPage = walletTransactionRepository.findByWalletId(
                wallet.getWalletId(),
                createPageRequest(page, size, "createdAt")
        );

        return toPagedResult(transactionPage.map(this::toWalletTransactionItemResult));
    }

    /**
     * 판매자 wallet에 아직 반영되지 않은 HELD escrow를 목록으로 반환한다.
     */
    @Override
    public PagedResult<PendingSellerIncomeItemResult> findAllPendingSellerIncomes(UUID memberId, int page, int size) {
        Page<Escrow> escrowPage = escrowRepository.findPendingBySellerMemberId(
                memberId,
                createPageRequest(page, size, "createdAt")
        );

        return toPagedResult(escrowPage.map(this::toPendingSellerIncomeItemResult));
    }

    /**
     * 모든 wallet 기반 조회의 공통 시작점이다.
     * wallet 미존재는 비즈니스 예외로 변환해 상위 응답 계층에서 일관되게 처리한다.
     */
    private Wallet findWallet(UUID memberId) {
        return walletRepository.findByMemberId(memberId)
                .orElseThrow(WalletNotFoundException::new);
    }

    /**
     * 목록 조회 공통 페이지 조건을 검증하고 최신순 Pageable을 생성한다.
     * <p>
     * 모든 조회 API가 동일한 page/size 정책을 사용하도록 guard를 한 곳에 모아서,
     * MAX_PAGE_SIZE 초과/음수 입력 같은 비즈니스 규칙을 일괄 적용한다.
     */
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

    /**
     * charge 목록 응답에서 필요한 필드만 추려 DTO로 변환한다.
     */
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

    /**
     * 환불 엔티티를 프론트 목록 응답 DTO로 변환한다.
     */
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

    /**
     * wallet transaction 엔티티를 프론트 표시용 DTO로 변환한다.
     */
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

    /**
     * 미정산 escrow를 판매자 대기 수익 DTO로 변환한다.
     */
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

    /**
     * Spring Data Page를 공통 PagedResult로 감싼다.
     */
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
