package com.example.member.infrastructure.client;

import com.example.member.application.port.out.MemberWithdrawalCheckPort;
import com.example.member.common.exception.MemberWithdrawalException;
import com.example.member.domain.entity.Member;
import com.example.member.domain.enumtype.MemberStatus;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import java.util.Iterator;
import java.util.Set;
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

    private static final Set<String> TERMINAL_ORDER_STATUSES = Set.of("COMPLETED", "CANCELED");
    private static final Set<String> BLOCKING_WITHDRAW_STATUSES = Set.of("REQUESTED", "PROCESSING");

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
            @Value("${services.auction.url:http://localhost:8086}") String auctionServiceUrl,
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

        validateBuyerOrders(authorizationHeader);

        if (member.getRole() == MemberRole.SELLER) {
            validateSellerProducts(authorizationHeader);
            validateSellerAuctions(authorizationHeader);
            validateSellerDeliveryCounts(authorizationHeader);
            validateSellerPendingIncomes(authorizationHeader);
            validateSellerWithdrawRequests(authorizationHeader);
            validateSellerSettlements(authorizationHeader);
            validateSellerPartialSettlementAvailability(authorizationHeader);
        }
    }

    private void validateBuyerOrders(String authorizationHeader) {
        JsonNode root = getJson(orderServiceUrl + "/api/orders?page=0&size=20", authorizationHeader);
        JsonNode orders = root.path("data").path("content");

        if (!orders.isArray()) {
            return;
        }

        for (JsonNode order : orders) {
            String status = order.path("status").asText("");
            if (!TERMINAL_ORDER_STATUSES.contains(status)) {
                throw new MemberWithdrawalException(
                        "MEMBER_WITHDRAWAL_ACTIVE_ORDER_EXISTS",
                        HttpStatus.CONFLICT,
                        "진행 중인 주문이 있어 탈퇴할 수 없습니다."
                );
            }
        }
    }

    private void validateSellerProducts(String authorizationHeader) {
        JsonNode root = getJson(productServiceUrl + "/api/products/seller?page=0&size=100", authorizationHeader);
        JsonNode products = root.path("content");

        if (!products.isArray()) {
            return;
        }

        for (JsonNode product : products) {
            String status = product.path("status").asText("");
            if ("ACTIVE".equals(status)) {
                throw new MemberWithdrawalException(
                        "MEMBER_WITHDRAWAL_ACTIVE_PRODUCT_EXISTS",
                        HttpStatus.CONFLICT,
                        "판매 중인 상품이 있어 탈퇴할 수 없습니다."
                );
            }
        }
    }

    private void validateSellerAuctions(String authorizationHeader) {
        validateSellerAuctionStatus(
                authorizationHeader,
                "WAITING",
                "MEMBER_WITHDRAWAL_ACTIVE_AUCTION_EXISTS",
                "진행 예정인 경매가 있어 탈퇴할 수 없습니다."
        );
        validateSellerAuctionStatus(
                authorizationHeader,
                "ONGOING",
                "MEMBER_WITHDRAWAL_ACTIVE_AUCTION_EXISTS",
                "진행 중인 경매가 있어 탈퇴할 수 없습니다."
        );
        validateSellerAuctionStatus(
                authorizationHeader,
                "PENDING_PAYMENT",
                "MEMBER_WITHDRAWAL_PENDING_AUCTION_PAYMENT_EXISTS",
                "결제 대기 중인 경매가 있어 탈퇴할 수 없습니다."
        );
    }

    private void validateSellerAuctionStatus(
            String authorizationHeader,
            String status,
            String code,
            String message
    ) {
        JsonNode root = getJson(
                auctionServiceUrl + "/api/auctions/seller?status=" + status + "&page=0&size=1",
                authorizationHeader
        );
        long totalElements = root.path("data").path("totalElements").asLong(0L);
        if (totalElements > 0) {
            throw new MemberWithdrawalException(code, HttpStatus.CONFLICT, message);
        }
    }

    private void validateSellerDeliveryCounts(String authorizationHeader) {
        JsonNode root = getJson(orderServiceUrl + "/api/deliveries/seller/counts", authorizationHeader);
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

    private void validateSellerPendingIncomes(String authorizationHeader) {
        JsonNode root = getJson(paymentServiceUrl + "/api/payments/seller/pending-incomes?page=0&size=1", authorizationHeader);
        JsonNode data = root.path("data");
        long totalElements = data.path("totalElements").asLong(0L);

        if (totalElements > 0) {
            throw new MemberWithdrawalException(
                    "MEMBER_WITHDRAWAL_PENDING_INCOME_EXISTS",
                    HttpStatus.CONFLICT,
                    "정산 대기 금액이 있어 탈퇴할 수 없습니다."
            );
        }
    }

    private void validateSellerWithdrawRequests(String authorizationHeader) {
        JsonNode root = getJson(paymentServiceUrl + "/api/payments/withdrawals?page=0&size=20", authorizationHeader);
        JsonNode items = root.path("data").path("items");

        if (!items.isArray()) {
            return;
        }

        Iterator<JsonNode> iterator = items.iterator();
        while (iterator.hasNext()) {
            JsonNode item = iterator.next();
            String status = item.path("status").asString("");
            if (BLOCKING_WITHDRAW_STATUSES.contains(status)) {
                throw new MemberWithdrawalException(
                        "MEMBER_WITHDRAWAL_PENDING_WITHDRAW_EXISTS",
                        HttpStatus.CONFLICT,
                        "처리 중인 출금 요청이 있어 탈퇴할 수 없습니다."
                );
            }
        }
    }

    private void validateSellerSettlements(String authorizationHeader) {
        validateSellerSettlementStatus(
                authorizationHeader,
                "PENDING",
                "MEMBER_WITHDRAWAL_PENDING_SETTLEMENT_EXISTS",
                "정산 대기 건이 있어 탈퇴할 수 없습니다."
        );
        validateSellerSettlementStatus(
                authorizationHeader,
                "PROCESSING",
                "MEMBER_WITHDRAWAL_PROCESSING_SETTLEMENT_EXISTS",
                "처리 중인 정산 건이 있어 탈퇴할 수 없습니다."
        );
    }

    private void validateSellerSettlementStatus(
            String authorizationHeader,
            String status,
            String code,
            String message
    ) {
        JsonNode root = getJson(
                settlementServiceUrl + "/api/settlements/seller?status=" + status + "&page=0&size=1",
                authorizationHeader
        );
        long totalElements = root.path("data").path("totalElements").asLong(0L);
        if (totalElements > 0) {
            throw new MemberWithdrawalException(code, HttpStatus.CONFLICT, message);
        }
    }

    private void validateSellerPartialSettlementAvailability(String authorizationHeader) {
        JsonNode root = getJson(
                settlementServiceUrl + "/api/settlements/seller/partial-settlements/available",
                authorizationHeader
        );
        JsonNode items = root.path("data");
        if (items.isArray() && !items.isEmpty()) {
            throw new MemberWithdrawalException(
                    "MEMBER_WITHDRAWAL_PARTIAL_SETTLEMENT_AVAILABLE",
                    HttpStatus.CONFLICT,
                    "부분 정산 가능한 금액이 남아 있어 탈퇴할 수 없습니다."
            );
        }
    }

    private JsonNode getJson(String url, String authorizationHeader) {
        try {
            String body = restClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);

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
