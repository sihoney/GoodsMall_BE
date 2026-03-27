package com.example.payment.application.usecase;

import com.example.payment.application.dto.ChargeDetailResult;
import com.example.payment.application.dto.ChargeListItemResult;
import com.example.payment.application.dto.ChargeRefundSummaryResult;
import com.example.payment.application.dto.PagedResult;
import com.example.payment.application.dto.PendingSellerIncomeItemResult;
import com.example.payment.application.dto.WalletSummaryResult;
import com.example.payment.application.dto.WalletTransactionItemResult;
import java.util.UUID;

public interface PaymentSearchUseCase {

    WalletSummaryResult findWalletSummary(UUID memberId);

    PagedResult<ChargeListItemResult> findAllCharges(UUID memberId, int page, int size);

    ChargeDetailResult findChargeDetail(UUID memberId, UUID chargeId);

    PagedResult<ChargeRefundSummaryResult> findAllRefunds(UUID memberId, int page, int size);

    PagedResult<WalletTransactionItemResult> findAllTransactions(UUID memberId, int page, int size);

    PagedResult<PendingSellerIncomeItemResult> findAllPendingSellerIncomes(UUID memberId, int page, int size);
}
