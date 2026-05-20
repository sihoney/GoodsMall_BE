package com.example.payment.payment.application.usecase;

import com.example.payment.wallet.application.dto.ChargeDetailResult;
import com.example.payment.wallet.application.dto.ChargeListItemResult;
import com.example.payment.escrow.application.dto.EscrowTransactionItemResult;
import com.example.payment.payment.application.dto.OrderPaymentDetailResult;
import com.example.payment.common.application.dto.PagedResult;
import com.example.payment.wallet.application.dto.PendingSellerIncomeItemResult;
import com.example.payment.wallet.application.dto.WalletSummaryResult;
import com.example.payment.wallet.application.dto.WalletTransactionItemResult;
import com.example.payment.wallet.application.dto.WithdrawListItemResult;
import java.util.List;
import java.util.UUID;

/**
 * payment 留덉씠?섏씠吏 議고쉶 ?좎뒪耳?댁뒪瑜??뺤쓽?쒕떎.
 * 議고쉶??紐⑤몢 memberId 湲곗??쇰줈 蹂몄씤 ?곗씠?곕쭔 諛섑솚?쒕떎.
 */
public interface PaymentSearchUseCase {

    /**
     * ?뚯썝??wallet ?붿빟 ?뺣낫瑜?議고쉶?쒕떎.
     */
    WalletSummaryResult findWalletSummary(UUID memberId);

    /**
     * ?뚯썝??異⑹쟾 紐⑸줉??理쒖떊?쒖쑝濡?議고쉶?쒕떎.
     */
    PagedResult<ChargeListItemResult> findAllCharges(UUID memberId, int page, int size);

    /**
     * ?뚯썝???④굔 charge ?곸꽭瑜?議고쉶?쒕떎.
     */
    ChargeDetailResult findChargeDetail(UUID memberId, UUID chargeId);

    /**
     * ?뚯썝 wallet??嫄곕옒 ?댁뿭??理쒖떊?쒖쑝濡?議고쉶?쒕떎.
     */
    PagedResult<WalletTransactionItemResult> findAllTransactions(UUID memberId, int page, int size);

    /**
     * ?먮ℓ??湲곗? 誘몄젙??escrow 紐⑸줉??理쒖떊?쒖쑝濡?議고쉶?쒕떎.
     */
    PagedResult<PendingSellerIncomeItemResult> findAllPendingSellerIncomes(UUID memberId, int page, int size);

    PagedResult<WithdrawListItemResult> findAllWithdrawRequests(UUID memberId, int page, int size);

    List<EscrowTransactionItemResult> findEscrowTransactionsByOrderId(UUID sellerMemberId, UUID orderId);

    OrderPaymentDetailResult findOrderPaymentByOrderId(UUID memberId, UUID orderId);
}
