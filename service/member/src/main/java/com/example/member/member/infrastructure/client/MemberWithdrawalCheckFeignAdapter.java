package com.example.member.member.infrastructure.client;

import com.example.member.member.application.port.out.MemberWithdrawalCheckPort;
import com.example.member.common.exception.BusinessException;
import com.example.member.member.domain.entity.Member;
import com.example.member.member.domain.enumtype.MemberStatus;
import com.example.member.member.exception.MemberErrorCode;
import com.example.member.member.infrastructure.client.dto.response.ApiResponse;
import com.example.member.member.infrastructure.client.dto.response.AuctionSellerBlockingSummaryResponse;
import com.example.member.member.infrastructure.client.dto.response.DeliveryStatusCountResponse;
import com.example.member.member.infrastructure.client.dto.response.MemberOrderWithdrawalSummaryResponse;
import com.example.member.member.infrastructure.client.dto.response.PaymentSellerWithdrawalSummaryResponse;
import com.example.member.member.infrastructure.client.dto.response.ProductSellerWithdrawalSummaryResponse;
import com.example.member.member.infrastructure.client.dto.response.SettlementSellerWithdrawalSummaryResponse;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import feign.FeignException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberWithdrawalCheckFeignAdapter implements MemberWithdrawalCheckPort {

    private final OrderWithdrawalClient orderWithdrawalClient;
    private final ProductWithdrawalClient productWithdrawalClient;
    private final AuctionWithdrawalClient auctionWithdrawalClient;
    private final PaymentWithdrawalClient paymentWithdrawalClient;
    private final SettlementWithdrawalClient settlementWithdrawalClient;

    @Override
    public void validateWithdrawable(Member member, String authorizationHeader) {
        if (member.getStatus() != MemberStatus.ACTIVE) {
            return;
        }

        // 구매자 주문 확인
        validateBuyerOrders(member.getMemberId());

        // 판매자 관련 확인
        if (member.getRole() == MemberRole.SELLER) {
            validateSellerProducts(member.getMemberId());
            validateSellerAuctions(member.getMemberId());
            validateSellerDeliveryCounts(member.getMemberId());
            validateSellerPaymentSummary(member.getMemberId());
            validateSellerSettlementSummary(member.getMemberId());
        }
    }

    private void validateBuyerOrders(UUID memberId) {
        try {
            ApiResponse<MemberOrderWithdrawalSummaryResponse> response =
                    orderWithdrawalClient.getMemberWithdrawalSummary(memberId);
            MemberOrderWithdrawalSummaryResponse data = requireData(response);
            if (data.hasActiveOrder()) {
                throw new BusinessException(MemberErrorCode.MEMBER_WITHDRAWAL_ACTIVE_ORDER_EXISTS);
            }
        } catch (FeignException exception) {
            throw unavailable("order member withdrawal summary", exception);
        }
    }

    private void validateSellerProducts(UUID memberId) {
        try {
            ProductSellerWithdrawalSummaryResponse response =
                    productWithdrawalClient.getSellerWithdrawalSummary(memberId);
            if (response.hasActiveProduct()) {
                throw new BusinessException(MemberErrorCode.MEMBER_WITHDRAWAL_ACTIVE_PRODUCT_EXISTS);
            }
        } catch (FeignException exception) {
            throw unavailable("product seller withdrawal summary", exception);
        }
    }

    private void validateSellerAuctions(UUID memberId) {
        try {
            ApiResponse<AuctionSellerBlockingSummaryResponse> response =
                    auctionWithdrawalClient.getSellerBlockingSummary(memberId);
            AuctionSellerBlockingSummaryResponse data = requireData(response);
            if (data.waiting() || data.ongoing()) {
                throw new BusinessException(MemberErrorCode.MEMBER_WITHDRAWAL_ACTIVE_AUCTION_EXISTS);
            }
            if (data.pendingPayment()) {
                throw new BusinessException(MemberErrorCode.MEMBER_WITHDRAWAL_PENDING_AUCTION_PAYMENT_EXISTS);
            }
        } catch (FeignException exception) {
            throw unavailable("auction seller blocking summary", exception);
        }
    }

    private void validateSellerDeliveryCounts(UUID memberId) {
        try {
            ApiResponse<DeliveryStatusCountResponse> response =
                    orderWithdrawalClient.getSellerDeliveryStatusCounts(memberId);
            DeliveryStatusCountResponse data = requireData(response);
            if (data.preparing() > 0 || data.shipped() > 0) {
                throw new BusinessException(MemberErrorCode.MEMBER_WITHDRAWAL_DELIVERY_IN_PROGRESS);
            }
        } catch (FeignException exception) {
            throw unavailable("order delivery status counts", exception);
        }
    }

    private void validateSellerPaymentSummary(UUID memberId) {
        try {
            ApiResponse<PaymentSellerWithdrawalSummaryResponse> response =
                    paymentWithdrawalClient.getSellerWithdrawalSummary(memberId);
            PaymentSellerWithdrawalSummaryResponse data = requireData(response);
            if (data.hasPendingIncome()) {
                throw new BusinessException(MemberErrorCode.MEMBER_WITHDRAWAL_PENDING_INCOME_EXISTS);
            }
            if (data.hasPendingWithdrawRequest()) {
                throw new BusinessException(MemberErrorCode.MEMBER_WITHDRAWAL_PENDING_WITHDRAW_EXISTS);
            }
        } catch (FeignException exception) {
            throw unavailable("payment seller withdrawal summary", exception);
        }
    }

    private void validateSellerSettlementSummary(UUID memberId) {
        try {
            ApiResponse<SettlementSellerWithdrawalSummaryResponse> response =
                    settlementWithdrawalClient.getSellerWithdrawalSummary(memberId);
            SettlementSellerWithdrawalSummaryResponse data = requireData(response);
            if (data.hasPendingSettlement()) {
                throw new BusinessException(MemberErrorCode.MEMBER_WITHDRAWAL_PENDING_SETTLEMENT_EXISTS);
            }
            if (data.hasProcessingSettlement()) {
                throw new BusinessException(MemberErrorCode.MEMBER_WITHDRAWAL_PROCESSING_SETTLEMENT_EXISTS);
            }
            if (data.hasPartialSettlementAvailable()) {
                throw new BusinessException(MemberErrorCode.MEMBER_WITHDRAWAL_PARTIAL_SETTLEMENT_AVAILABLE);
            }
        } catch (FeignException exception) {
            throw unavailable("settlement seller withdrawal summary", exception);
        }
    }

    private <T> T requireData(ApiResponse<T> response) {
        if (response == null || !response.success() || response.data() == null) {
            throw new BusinessException(MemberErrorCode.MEMBER_WITHDRAWAL_CHECK_UNAVAILABLE);
        }
        return response.data();
    }

    private BusinessException unavailable(String operation, FeignException exception) {
        log.warn("Failed to query {} for member withdrawal validation.", operation, exception);
        return new BusinessException(MemberErrorCode.MEMBER_WITHDRAWAL_CHECK_UNAVAILABLE);
    }
}
