package com.example.member.member.infrastructure.client;

import com.example.member.member.application.port.out.MemberWithdrawalCheckPort;
import com.example.member.member.exception.MemberWithdrawalException;
import com.example.member.member.domain.entity.Member;
import com.example.member.member.domain.enumtype.MemberStatus;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberWithdrawalCheckFeignAdapter implements MemberWithdrawalCheckPort {

    private static final String CHECK_UNAVAILABLE_MESSAGE =
            "회원 탈퇴 가능 여부를 확인할 수 없습니다. 잠시 후 다시 시도해 주세요.";

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
                throw new MemberWithdrawalException(
                        "MEMBER_WITHDRAWAL_ACTIVE_ORDER_EXISTS",
                        HttpStatus.CONFLICT,
                        "진행 중인 주문이 있어 탈퇴할 수 없습니다."
                );
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
                throw new MemberWithdrawalException(
                        "MEMBER_WITHDRAWAL_ACTIVE_PRODUCT_EXISTS",
                        HttpStatus.CONFLICT,
                        "판매 중인 상품이 있어 탈퇴할 수 없습니다."
                );
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
                throw new MemberWithdrawalException(
                        "MEMBER_WITHDRAWAL_ACTIVE_AUCTION_EXISTS",
                        HttpStatus.CONFLICT,
                        "진행 중인 경매가 있어 탈퇴할 수 없습니다."
                );
            }
            if (data.pendingPayment()) {
                throw new MemberWithdrawalException(
                        "MEMBER_WITHDRAWAL_PENDING_AUCTION_PAYMENT_EXISTS",
                        HttpStatus.CONFLICT,
                        "결제 대기 중인 경매가 있어 탈퇴할 수 없습니다."
                );
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
                throw new MemberWithdrawalException(
                        "MEMBER_WITHDRAWAL_DELIVERY_IN_PROGRESS",
                        HttpStatus.CONFLICT,
                        "진행 중인 배송 건이 있어 탈퇴할 수 없습니다."
                );
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
                throw new MemberWithdrawalException(
                        "MEMBER_WITHDRAWAL_PENDING_INCOME_EXISTS",
                        HttpStatus.CONFLICT,
                        "정산 대기 금액이 있어 탈퇴할 수 없습니다."
                );
            }
            if (data.hasPendingWithdrawRequest()) {
                throw new MemberWithdrawalException(
                        "MEMBER_WITHDRAWAL_PENDING_WITHDRAW_EXISTS",
                        HttpStatus.CONFLICT,
                        "처리 중인 출금 요청이 있어 탈퇴할 수 없습니다."
                );
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
                throw new MemberWithdrawalException(
                        "MEMBER_WITHDRAWAL_PENDING_SETTLEMENT_EXISTS",
                        HttpStatus.CONFLICT,
                        "정산 대기 건이 있어 탈퇴할 수 없습니다."
                );
            }
            if (data.hasProcessingSettlement()) {
                throw new MemberWithdrawalException(
                        "MEMBER_WITHDRAWAL_PROCESSING_SETTLEMENT_EXISTS",
                        HttpStatus.CONFLICT,
                        "처리 중인 정산 건이 있어 탈퇴할 수 없습니다."
                );
            }
            if (data.hasPartialSettlementAvailable()) {
                throw new MemberWithdrawalException(
                        "MEMBER_WITHDRAWAL_PARTIAL_SETTLEMENT_AVAILABLE",
                        HttpStatus.CONFLICT,
                        "부분 정산 가능한 금액이 남아 있어 탈퇴할 수 없습니다."
                );
            }
        } catch (FeignException exception) {
            throw unavailable("settlement seller withdrawal summary", exception);
        }
    }

    private <T> T requireData(ApiResponse<T> response) {
        if (response == null || !response.success() || response.data() == null) {
            throw new MemberWithdrawalException(
                    "MEMBER_WITHDRAWAL_CHECK_UNAVAILABLE",
                    HttpStatus.SERVICE_UNAVAILABLE,
                    CHECK_UNAVAILABLE_MESSAGE
            );
        }
        return response.data();
    }

    private MemberWithdrawalException unavailable(String operation, FeignException exception) {
        log.warn("Failed to query {} for member withdrawal validation.", operation, exception);
        return new MemberWithdrawalException(
                "MEMBER_WITHDRAWAL_CHECK_UNAVAILABLE",
                HttpStatus.SERVICE_UNAVAILABLE,
                CHECK_UNAVAILABLE_MESSAGE
        );
    }
}
