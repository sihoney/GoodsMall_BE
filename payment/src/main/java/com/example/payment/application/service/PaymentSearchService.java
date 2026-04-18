package com.example.payment.application.service;

import com.example.payment.application.dto.ChargeDetailResult;
import com.example.payment.application.dto.ChargeListItemResult;
import com.example.payment.application.dto.ChargeRefundSummaryResult;
import com.example.payment.application.dto.EscrowTransactionItemResult;
import com.example.payment.application.dto.PagedResult;
import com.example.payment.application.dto.PendingSellerIncomeItemResult;
import com.example.payment.application.dto.WalletSummaryResult;
import com.example.payment.application.dto.WalletTransactionItemResult;
import com.example.payment.application.dto.WithdrawListItemResult;
import com.example.payment.application.usecase.PaymentSearchUseCase;
import com.example.payment.common.exception.ChargeNotFoundException;
import com.example.payment.common.exception.WalletNotFoundException;
import com.example.payment.domain.entity.Charge;
import com.example.payment.domain.entity.ChargeRefund;
import com.example.payment.domain.entity.Escrow;
import com.example.payment.domain.entity.EscrowTransaction;
import com.example.payment.domain.entity.Wallet;
import com.example.payment.domain.entity.WalletTransaction;
import com.example.payment.domain.entity.WithdrawRequest;
import com.example.payment.domain.repository.ChargeRefundRepository;
import com.example.payment.domain.repository.ChargeRepository;
import com.example.payment.domain.repository.EscrowRepository;
import com.example.payment.domain.repository.EscrowTransactionRepository;
import com.example.payment.domain.repository.WalletRepository;
import com.example.payment.domain.repository.WalletTransactionRepository;
import com.example.payment.domain.repository.WithdrawRequestRepository;
import java.util.List;
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
    private final EscrowTransactionRepository escrowTransactionRepository;
    private final WithdrawRequestRepository withdrawRequestRepository;

    public PaymentSearchService(
            WalletRepository walletRepository,
            ChargeRepository chargeRepository,
            ChargeRefundRepository chargeRefundRepository,
            WalletTransactionRepository walletTransactionRepository,
            EscrowRepository escrowRepository,
            EscrowTransactionRepository escrowTransactionRepository,
            WithdrawRequestRepository withdrawRequestRepository
    ) {
        this.walletRepository = walletRepository;
        this.chargeRepository = chargeRepository;
        this.chargeRefundRepository = chargeRefundRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.escrowRepository = escrowRepository;
        this.escrowTransactionRepository = escrowTransactionRepository;
        this.withdrawRequestRepository = withdrawRequestRepository;
    }

    /**
     * 화면에 표시할 예치금 금액을 알려주는 메서드
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
     * 단건 charge 상세를 조회한다.
     * 목록에서 특정 충전건을 눌렀을 때 바로 상태를 판단할 수 있도록,
     * 해당 충전의 최신 환불 1건을 함께 내려준다.
     * 또한 memberId 조건으로 본인 데이터만 조회되도록 제한한다.
     */
    @Override
    public ChargeDetailResult findChargeDetail(UUID memberId, UUID chargeId) {
        Charge charge = chargeRepository.findByChargeIdAndMemberId(chargeId, memberId)
                .orElseThrow(ChargeNotFoundException::new);

        ChargeRefundSummaryResult latestRefund = chargeRefundRepository.findTopByChargeIdOrderByRequestedAtDesc(chargeId)
                .map(this::toChargeRefundSummaryResult)
                .orElse(null);

        // todo:  front에 전달될 값들이 전부 필요한지 검증하여 필요한 데이터만 보내도록 검증할것.
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
        // todo: 조회 책임 분리 vs memberId 기준 직접 조회 방식 고려할 것.
        // todo: wallet을 조회해 없을 경우 방어기재를 넣을 수 있다.
        Wallet wallet = findWallet(memberId);
        Page<WalletTransaction> transactionPage = walletTransactionRepository.findByWalletId(
                wallet.getWalletId(),
                createPageRequest(page, size, "createdAt")
        );
        // Page에서 map()은 요소 하나하나를 다른 타입으로 변경하나 페이지 정보는 유지한다.
        // Page<WalletTransaction> -> Page<WalletTransactionItemResult>로 변환된다.
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

    @Override
    public PagedResult<WithdrawListItemResult> findAllWithdrawRequests(UUID memberId, int page, int size) {
        Page<WithdrawRequest> withdrawRequestPage = withdrawRequestRepository.findByMemberId(
                memberId,
                createPageRequest(page, size, "requestedAt")
        );

        return toPagedResult(withdrawRequestPage.map(this::toWithdrawListItemResult));
    }

    @Override
    public List<EscrowTransactionItemResult> findEscrowTransactionsByOrderId(UUID sellerMemberId, UUID orderId) {
        return escrowTransactionRepository.findAllByOrderIdAndSellerMemberIdOrderByOccurredAtAsc(orderId, sellerMemberId)
                .stream()
                .map(this::toEscrowTransactionItemResult)
                .toList();
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
        // PageRequest는 Pageable의 구현체로, 페이지 번호(page)와 페이지 크기(size), 정렬 조건(sortBy)을 함께 전달한다.
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
                charge.getTossBankCode(),
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
                withdrawRequest.getStatus(),
                withdrawRequest.getRequestedAt(),
                withdrawRequest.getProcessedAt()
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
