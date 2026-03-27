package com.example.payment.application.usecase;

import com.example.payment.application.dto.ChargeDetailResult;
import com.example.payment.application.dto.ChargeListItemResult;
import com.example.payment.application.dto.ChargeRefundSummaryResult;
import com.example.payment.application.dto.PagedResult;
import com.example.payment.application.dto.PendingSellerIncomeItemResult;
import com.example.payment.application.dto.WalletSummaryResult;
import com.example.payment.application.dto.WalletTransactionItemResult;
import java.util.UUID;

public interface PaymentQueryUseCase {

    WalletSummaryResult getWalletSummary(UUID memberId);

    PagedResult<ChargeListItemResult> getCharges(UUID memberId, int page, int size);

    ChargeDetailResult getChargeDetail(UUID memberId, UUID chargeId);

    PagedResult<ChargeRefundSummaryResult> getRefunds(UUID memberId, int page, int size);

    PagedResult<WalletTransactionItemResult> getTransactions(UUID memberId, int page, int size);

    PagedResult<PendingSellerIncomeItemResult> getPendingSellerIncomes(UUID memberId, int page, int size);
}
