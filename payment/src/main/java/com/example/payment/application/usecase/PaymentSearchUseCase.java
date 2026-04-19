package com.example.payment.application.usecase;

import com.example.payment.application.dto.ChargeDetailResult;
import com.example.payment.application.dto.ChargeListItemResult;
import com.example.payment.application.dto.EscrowTransactionItemResult;
import com.example.payment.application.dto.PagedResult;
import com.example.payment.application.dto.PendingSellerIncomeItemResult;
import com.example.payment.application.dto.WalletSummaryResult;
import com.example.payment.application.dto.WalletTransactionItemResult;
import com.example.payment.application.dto.WithdrawListItemResult;
import java.util.List;
import java.util.UUID;

/**
 * payment 마이페이지 조회 유스케이스를 정의한다.
 * 조회는 모두 memberId 기준으로 본인 데이터만 반환한다.
 */
public interface PaymentSearchUseCase {

    /**
     * 회원의 wallet 요약 정보를 조회한다.
     */
    WalletSummaryResult findWalletSummary(UUID memberId);

    /**
     * 회원의 충전 목록을 최신순으로 조회한다.
     */
    PagedResult<ChargeListItemResult> findAllCharges(UUID memberId, int page, int size);

    /**
     * 회원의 단건 charge 상세를 조회한다.
     */
    ChargeDetailResult findChargeDetail(UUID memberId, UUID chargeId);

    /**
     * 회원 wallet의 거래 내역을 최신순으로 조회한다.
     */
    PagedResult<WalletTransactionItemResult> findAllTransactions(UUID memberId, int page, int size);

    /**
     * 판매자 기준 미정산 escrow 목록을 최신순으로 조회한다.
     */
    PagedResult<PendingSellerIncomeItemResult> findAllPendingSellerIncomes(UUID memberId, int page, int size);

    PagedResult<WithdrawListItemResult> findAllWithdrawRequests(UUID memberId, int page, int size);

    List<EscrowTransactionItemResult> findEscrowTransactionsByOrderId(UUID sellerMemberId, UUID orderId);
}
