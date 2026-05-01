package com.example.order.application.service;

import com.example.order.application.port.dto.response.PaymentRefundResult;
import com.example.order.application.processor.PaymentProcessor;
import com.example.order.application.usecase.OrderCancelUseCase;
import com.example.order.common.exception.CustomException;
import com.example.order.common.exception.ErrorCode;
import com.example.order.domain.entity.Claim;
import com.example.order.domain.entity.Order;
import com.example.order.domain.entity.OrderItem;
import com.example.order.domain.entity.OutboxEvent;
import com.example.order.domain.entity.ReturnRequest;
import com.example.order.domain.enumtype.ClaimType;
import com.example.order.domain.enumtype.PaymentStatus;
import com.example.order.domain.enumtype.PickupType;
import com.example.order.domain.enumtype.ResponsibilityType;
import com.example.order.domain.repository.ClaimRepository;
import com.example.order.domain.repository.OrderRepository;
import com.example.order.domain.repository.OutboxRepository;
import com.example.order.domain.repository.ReturnRequestRepository;
import com.example.order.infrastructure.kafka.KafkaTopics;
import com.example.order.infrastructure.kafka.event.OrderCanceledEvent;
import com.example.order.infrastructure.kafka.event.OrderReturnRequestedEvent;
import com.example.order.presentation.dto.request.ClaimItemRequest;
import com.example.order.presentation.dto.request.OrderCancelRequest;
import com.example.order.presentation.dto.response.OrderCancelResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderCancelService implements OrderCancelUseCase {

    private static final String MOCK_CARRIER = "MOCK_CARRIER";
    private static final String MOCK_RETURN_ADDRESS = "TBD";

    private final OrderRepository orderRepository;
    private final ClaimRepository claimRepository;
    private final ReturnRequestRepository returnRequestRepository;
    private final PaymentProcessor paymentProcessor;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public OrderCancelResponse cancelOrder(UUID orderId, UUID memberId, OrderCancelRequest request) {
        Order order = findOrder(orderId);
        validateOrderOwner(order, memberId);

        Map<UUID, OrderItem> itemMap = toItemMap(order);
        List<RequestedItem> requested = resolveRequestedItems(request.items(), itemMap);

        List<RequestedItem> toCancel = filterCancelable(requested);
        List<RequestedItem> toReturn = filterReturnable(requested);

        validateReturnNotDuplicated(toReturn);

        List<OrderItem> canceledItems = applyCancel(toCancel);
        applyReturnRequest(toReturn);

        List<Claim> claims = new ArrayList<>(buildClaims(toCancel, ClaimType.CANCEL, request, request.requesterType().toResponsibility()));
        claims.addAll(buildClaims(toReturn, ClaimType.RETURN, request, null));
        claimRepository.saveAll(claims);

        List<ReturnRequest> returnRequests = createReturnRequestsWithMockPickup(toReturn, claims);
        returnRequestRepository.saveAll(returnRequests);

        BigDecimal refundedAmount = BigDecimal.ZERO;
        LocalDateTime processedAt = LocalDateTime.now();

        if (!canceledItems.isEmpty()) {
            order.cancel(hasRemainingItems(order, canceledItems));
            PaymentRefundResult refundResult = paymentProcessor.refund(order, canceledItems, firstReason(toCancel));
            validateRefundResult(refundResult);
            refundedAmount = refundResult.refundedAmount();
            processedAt = LocalDateTime.ofInstant(refundResult.canceledAt(), ZoneId.systemDefault());
            publishCanceledEvent(order, canceledItems, refundResult);
        }

        if (!toReturn.isEmpty()) {
            publishReturnRequestedEvent(order, toReturn.stream().map(RequestedItem::item).toList());
        }

        return new OrderCancelResponse(
                order.getOrderId(),
                refundedAmount,
                canceledItems.size(),
                toReturn.size(),
                processedAt
        );
    }

    private Order findOrder(UUID orderId) {
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
    }

    private void validateOrderOwner(Order order, UUID memberId) {
        if (!order.getBuyerId().equals(memberId)) {
            throw new CustomException(ErrorCode.ORDER_FORBIDDEN);
        }
    }

    private Map<UUID, OrderItem> toItemMap(Order order) {
        return order.getItems().stream()
                .collect(Collectors.toMap(OrderItem::getOrderItemId, Function.identity()));
    }

    private List<RequestedItem> resolveRequestedItems(List<ClaimItemRequest> requests, Map<UUID, OrderItem> itemMap) {
        return requests.stream()
                .map(req -> {
                    OrderItem item = itemMap.get(req.orderItemId());
                    if (item == null) {
                        throw new CustomException(ErrorCode.ORDER_ITEM_NOT_FOUND);
                    }
                    log.info("[cancelOrder] requested orderItemId={}, status={}, canCancel={}, canReturn={}",
                            item.getOrderItemId(), item.getStatus(), item.canCancel(), item.canReturn());
                    if (!item.canCancel() && !item.canReturn()) {
                        throw new CustomException(ErrorCode.ORDER_ITEM_NOT_CLAIMABLE);
                    }
                    return new RequestedItem(item, req.reason(), req.detailReason());
                })
                .toList();
    }

    private List<RequestedItem> filterCancelable(List<RequestedItem> requested) {
        return requested.stream()
                .filter(r -> r.item().canCancel())
                .toList();
    }

    private List<RequestedItem> filterReturnable(List<RequestedItem> requested) {
        return requested.stream()
                .filter(r -> r.item().canReturn())
                .toList();
    }

    private void validateReturnNotDuplicated(List<RequestedItem> toReturn) {
        for (RequestedItem r : toReturn) {
            if (returnRequestRepository.existsActiveByOrderItemId(r.item().getOrderItemId())) {
                log.warn("[cancelOrder] RETURN_ALREADY_REQUESTED blocked. orderItemId={}, itemStatus={}",
                        r.item().getOrderItemId(), r.item().getStatus());
                throw new CustomException(ErrorCode.RETURN_ALREADY_REQUESTED);
            }
        }
    }

    private List<OrderItem> applyCancel(List<RequestedItem> toCancel) {
        return toCancel.stream()
                .peek(r -> r.item().cancel())
                .map(RequestedItem::item)
                .toList();
    }

    private void applyReturnRequest(List<RequestedItem> toReturn) {
        toReturn.forEach(r -> r.item().requestReturn());
    }

    private boolean hasRemainingItems(Order order, List<OrderItem> canceledItems) {
        return order.getItems().size() != canceledItems.size();
    }

    private List<Claim> buildClaims(
            List<RequestedItem> requested,
            ClaimType type,
            OrderCancelRequest request,
            ResponsibilityType responsibilityType
    ) {
        return requested.stream()
                .map(r -> Claim.create(
                        r.item(),
                        r.item().getSellerId(),
                        type,
                        r.reason(),
                        r.detailReason(),
                        request.requesterType(),
                        responsibilityType
                ))
                .toList();
    }

    private List<ReturnRequest> createReturnRequestsWithMockPickup(List<RequestedItem> toReturn, List<Claim> claims) {
        Map<UUID, Claim> claimByOrderItemId = claims.stream()
                .filter(c -> c.getType() == ClaimType.RETURN)
                .collect(Collectors.toMap(c -> c.getOrderItem().getOrderItemId(), Function.identity()));

        return toReturn.stream()
                .map(r -> {
                    Claim claim = claimByOrderItemId.get(r.item().getOrderItemId());
                    ReturnRequest returnRequest = ReturnRequest.create(
                            claim,
                            r.item(),
                            r.item().getSellerId(),
                            PickupType.PICKUP_REQUEST,
                            MOCK_RETURN_ADDRESS
                    );
                    // TODO: 실제 운영에선 carrier API 연동으로 송장 발급 + 상태 전이
                    returnRequest.registerTracking(MOCK_CARRIER, generateMockTrackingNumber());
                    returnRequest.requestPickup();
                    returnRequest.confirmPickup();
                    return returnRequest;
                })
                .toList();
    }

    private String generateMockTrackingNumber() {
        return "MOCK-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

    private String firstReason(List<RequestedItem> items) {
        return items.isEmpty() ? "" : items.get(0).reason();
    }

    private void validateRefundResult(PaymentRefundResult refundResult) {
        if (refundResult.status() != PaymentStatus.SUCCESS) {
            throw new CustomException(ErrorCode.REFUND_FAILED);
        }
    }

    private void publishCanceledEvent(Order order, List<OrderItem> canceledItems, PaymentRefundResult refundResult) {
        try {
            String payload = objectMapper.writeValueAsString(OrderCanceledEvent.envelopeOf(order, canceledItems, refundResult.canceledAt()));
            outboxRepository.save(OutboxEvent.create(KafkaTopics.ORDER_CANCELED, order.getOrderId().toString(), payload));
        } catch (Exception e) {
            log.error("OrderCanceledEvent Outbox 저장 실패. orderId={}", order.getOrderId(), e);
        }
    }

    private void publishReturnRequestedEvent(Order order, List<OrderItem> returnItems) {
        try {
            String payload = objectMapper.writeValueAsString(OrderReturnRequestedEvent.envelopeOf(order, returnItems));
            outboxRepository.save(OutboxEvent.create(KafkaTopics.ORDER_RETURN_REQUESTED, order.getOrderId().toString(), payload));
        } catch (Exception e) {
            log.error("OrderReturnRequestedEvent Outbox 저장 실패. orderId={}", order.getOrderId(), e);
        }
    }

    private record RequestedItem(OrderItem item, String reason, String detailReason) {
    }
}
