package com.example.member.infrastructure.client;

import com.example.member.application.port.out.MemberWithdrawalCheckPort;
import com.example.member.common.exception.MemberWithdrawalException;
import com.example.member.domain.entity.Member;
import com.example.member.domain.enumtype.MemberStatus;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class MemberWithdrawalCheckHttpAdapter implements MemberWithdrawalCheckPort {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String orderServiceUrl;
    private final String paymentServiceUrl;
    private final String productServiceUrl;
    private final String auctionServiceUrl;
    private final String settlementServiceUrl;

    public MemberWithdrawalCheckHttpAdapter(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${services.order.url:http://localhost:8084}") String orderServiceUrl,
            @Value("${services.payment.url:http://localhost:8082}") String paymentServiceUrl,
            @Value("${services.product.url:http://localhost:8081}") String productServiceUrl,
            @Value("${services.auction.url:http://localhost:8090}") String auctionServiceUrl,
            @Value("${services.settlement.url:http://localhost:8085}") String settlementServiceUrl
    ) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
        this.orderServiceUrl = orderServiceUrl;
        this.paymentServiceUrl = paymentServiceUrl;
        this.productServiceUrl = productServiceUrl;
        this.auctionServiceUrl = auctionServiceUrl;
        this.settlementServiceUrl = settlementServiceUrl;
    }

    @Override
    public void validateWithdrawable(Member member, String authorizationHeader) {
        if (member.getStatus() != MemberStatus.ACTIVE) {
            return;
        }

        validateBuyerOrders(member);

        if (member.getRole() == MemberRole.SELLER) {
            validateSellerProducts(member);
            // validateSellerAuctions(member);
            validateSellerDeliveryCounts(member);
            validateSellerPaymentSummary(member);
            validateSellerSettlementSummary(member);
        }
    }

    private void validateBuyerOrders(Member member) {
        JsonNode root = getJson(
                orderServiceUrl + "/internal/orders/members/" + member.getMemberId() + "/withdrawal-summary",
                null
        );
        if (root.path("data").path("hasActiveOrder").asBoolean(false)) {
            throw new MemberWithdrawalException(
                    "MEMBER_WITHDRAWAL_ACTIVE_ORDER_EXISTS",
                    HttpStatus.CONFLICT,
                    "진행 중인 주문이 있어 탈퇴할 수 없습니다."
            );
        }
    }

    private void validateSellerProducts(Member member) {
        JsonNode root = getJson(
                productServiceUrl + "/internal/products/sellers/" + member.getMemberId() + "/withdrawal-summary",
                null
        );
        if (root.path("hasActiveProduct").asBoolean(false)) {
            throw new MemberWithdrawalException(
                    "MEMBER_WITHDRAWAL_ACTIVE_PRODUCT_EXISTS",
                    HttpStatus.CONFLICT,
                    "판매 중인 상품이 있어 탈퇴할 수 없습니다."
            );
        }
    }

    private void validateSellerAuctions(Member member) {
        JsonNode root = getJson(
                auctionServiceUrl + "/internal/auctions/sellers/" + member.getMemberId() + "/blocking-summary",
                null
        );
        JsonNode data = root.path("data");

        if (data.path("waiting").asBoolean(false) || data.path("ongoing").asBoolean(false)) {
            throw new MemberWithdrawalException(
                    "MEMBER_WITHDRAWAL_ACTIVE_AUCTION_EXISTS",
                    HttpStatus.CONFLICT,
                    "진행 중인 경매가 있어 탈퇴할 수 없습니다."
            );
        }

        if (data.path("pendingPayment").asBoolean(false)) {
            throw new MemberWithdrawalException(
                    "MEMBER_WITHDRAWAL_PENDING_AUCTION_PAYMENT_EXISTS",
                    HttpStatus.CONFLICT,
                    "결제 대기 중인 경매가 있어 탈퇴할 수 없습니다."
            );
        }
    }

    private void validateSellerDeliveryCounts(Member member) {
        JsonNode root = getJson(
                orderServiceUrl + "/internal/deliveries/sellers/" + member.getMemberId() + "/status-counts",
                null
        );
        JsonNode counts = root.path("data");
        long preparing = counts.path("preparing").asLong(0L);
        long shipped = counts.path("shipped").asLong(0L);

        if (preparing > 0 || shipped > 0) {
            throw new MemberWithdrawalException(
                    "MEMBER_WITHDRAWAL_DELIVERY_IN_PROGRESS",
                    HttpStatus.CONFLICT,
                    "진행 중인 배송 건이 있어 탈퇴할 수 없습니다."
            );
        }
    }

    private void validateSellerPaymentSummary(Member member) {
        JsonNode root = getJson(
                paymentServiceUrl + "/internal/payments/sellers/" + member.getMemberId() + "/withdrawal-summary",
                null
        );
        JsonNode items = root.path("data");

        if (items.path("hasPendingIncome").asBoolean(false)) {
            throw new MemberWithdrawalException(
                    "MEMBER_WITHDRAWAL_PENDING_INCOME_EXISTS",
                    HttpStatus.CONFLICT,
                    "정산 대기 금액이 있어 탈퇴할 수 없습니다."
            );
        }

        if (items.path("hasPendingWithdrawRequest").asBoolean(false)) {
            throw new MemberWithdrawalException(
                    "MEMBER_WITHDRAWAL_PENDING_WITHDRAW_EXISTS",
                    HttpStatus.CONFLICT,
                    "처리 중인 출금 요청이 있어 탈퇴할 수 없습니다."
            );
        }
    }

    private void validateSellerSettlementSummary(Member member) {
        JsonNode root = getJson(
                settlementServiceUrl + "/internal/settlements/sellers/" + member.getMemberId() + "/withdrawal-summary",
                null
        );
        JsonNode data = root.path("data");

        if (data.path("hasPendingSettlement").asBoolean(false)) {
            throw new MemberWithdrawalException(
                    "MEMBER_WITHDRAWAL_PENDING_SETTLEMENT_EXISTS",
                    HttpStatus.CONFLICT,
                    "정산 대기 건이 있어 탈퇴할 수 없습니다."
            );
        }

        if (data.path("hasProcessingSettlement").asBoolean(false)) {
            throw new MemberWithdrawalException(
                    "MEMBER_WITHDRAWAL_PROCESSING_SETTLEMENT_EXISTS",
                    HttpStatus.CONFLICT,
                    "처리 중인 정산 건이 있어 탈퇴할 수 없습니다."
            );
        }

        if (data.path("hasPartialSettlementAvailable").asBoolean(false)) {
            throw new MemberWithdrawalException(
                    "MEMBER_WITHDRAWAL_PARTIAL_SETTLEMENT_AVAILABLE",
                    HttpStatus.CONFLICT,
                    "부분 정산 가능한 금액이 남아 있어 탈퇴할 수 없습니다."
            );
        }
    }

    private JsonNode getJson(String url, String authorizationHeader) {
        try {
            RestClient.RequestHeadersSpec<?> request = restClient.get()
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON);

            if (authorizationHeader != null && !authorizationHeader.isBlank()) {
                request = request.header(HttpHeaders.AUTHORIZATION, authorizationHeader);
            }

            String body = request.retrieve().body(String.class);

            if (body == null || body.isBlank()) {
                return objectMapper.createObjectNode();
            }

            return objectMapper.readTree(body);
        } catch (RestClientException exception) {
            log.warn("회원탈퇴 차단 조건 조회 실패. url={}", url, exception);
            throw new MemberWithdrawalException(
                    "MEMBER_WITHDRAWAL_CHECK_UNAVAILABLE",
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "회원탈퇴 가능 여부를 확인할 수 없습니다. 잠시 후 다시 시도해 주세요."
            );
        } catch (Exception exception) {
            log.warn("회원탈퇴 차단 조건 응답 파싱 실패. url={}", url, exception);
            throw new MemberWithdrawalException(
                    "MEMBER_WITHDRAWAL_CHECK_UNAVAILABLE",
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "회원탈퇴 가능 여부를 확인할 수 없습니다. 잠시 후 다시 시도해 주세요."
            );
        }
    }
}
