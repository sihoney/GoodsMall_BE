package com.example.order.application.service;

import com.example.order.application.port.dto.response.PaymentRefundResult;
import com.example.order.application.processor.PaymentProcessor;
import com.example.order.application.usecase.ReturnInspectUseCase;
import com.example.order.common.exception.CustomException;
import com.example.order.common.exception.ErrorCode;
import com.example.order.domain.entity.Claim;
import com.example.order.domain.entity.Order;
import com.example.order.domain.entity.OrderItem;
import com.example.order.domain.entity.OutboxEvent;
import com.example.order.domain.entity.ReturnRequest;
import com.example.order.domain.enumtype.InspectionResult;
import com.example.order.domain.enumtype.OrderItemStatus;
import com.example.order.domain.enumtype.PaymentStatus;
import com.example.order.domain.enumtype.ReturnRequestStatus;
import com.example.order.domain.repository.OutboxRepository;
import com.example.order.domain.repository.ReturnRequestRepository;
import com.example.order.infrastructure.kafka.KafkaTopics;
import com.example.order.infrastructure.kafka.event.OrderReturnCompletedEvent;
import com.example.order.presentation.dto.request.ReturnInspectRequest;
import com.example.order.presentation.dto.response.ReturnInspectResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReturnInspectService implements ReturnInspectUseCase {

    private final ReturnRequestRepository returnRequestRepository;
    private final PaymentProcessor paymentProcessor;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ReturnInspectResponse inspect(UUID returnRequestId, UUID sellerMemberId, ReturnInspectRequest request) {
        validateRequest(request);

        ReturnRequest returnRequest = returnRequestRepository.findById(returnRequestId)
                .orElseThrow(() -> new CustomException(ErrorCode.RETURN_REQUEST_NOT_FOUND));

        validateSellerAuthority(returnRequest, sellerMemberId);
        validateInspectable(returnRequest);

        if (request.inspectionResult() == InspectionResult.PASS) {
            return processPass(returnRequest, request);
        }
        return processFail(returnRequest, request);
    }

    private void validateRequest(ReturnInspectRequest request) {
        if (request.inspectionResult() == InspectionResult.PASS && request.responsibilityType() == null) {
            throw new CustomException(ErrorCode.INVALID_INSPECTION_REQUEST);
        }
        if (request.inspectionResult() == InspectionResult.FAIL
                && (request.rejectReason() == null || request.rejectReason().isBlank())) {
            throw new CustomException(ErrorCode.INVALID_INSPECTION_REQUEST);
        }
    }

    private void validateSellerAuthority(ReturnRequest returnRequest, UUID sellerMemberId) {
        if (!returnRequest.getSellerId().equals(sellerMemberId)) {
            throw new CustomException(ErrorCode.ORDER_FORBIDDEN);
        }
    }

    private void validateInspectable(ReturnRequest returnRequest) {
        if (returnRequest.getStatus() != ReturnRequestStatus.RECEIVED) {
            throw new CustomException(ErrorCode.RETURN_NOT_INSPECTABLE);
        }
    }

    private ReturnInspectResponse processPass(ReturnRequest returnRequest, ReturnInspectRequest request) {
        Claim claim = returnRequest.getClaim();
        OrderItem orderItem = returnRequest.getOrderItem();
        Order order = orderItem.getOrder();

        claim.assignResponsibility(request.responsibilityType());

        PaymentRefundResult refundResult = paymentProcessor.sellerRefund(
                order,
                List.of(orderItem),
                claim.getClaimId(),
                claim.getReason()
        );
        if (refundResult.status() != PaymentStatus.SUCCESS) {
            throw new CustomException(ErrorCode.REFUND_FAILED);
        }

        orderItem.completeReturn();
        updateOrderStatus(order);

        returnRequest.completeInspection(InspectionResult.PASS);
        returnRequest.complete();
        claim.complete();

        publishReturnCompletedEvent(order, returnRequest, orderItem, refundResult.refundedAmount());

        return new ReturnInspectResponse(
                returnRequest.getReturnRequestId(),
                returnRequest.getStatus(),
                InspectionResult.PASS,
                refundResult.refundedAmount(),
                LocalDateTime.ofInstant(refundResult.canceledAt(), ZoneId.systemDefault())
        );
    }

    private ReturnInspectResponse processFail(ReturnRequest returnRequest, ReturnInspectRequest request) {
        Claim claim = returnRequest.getClaim();
        if (request.responsibilityType() != null) {
            claim.assignResponsibility(request.responsibilityType());
        }

        returnRequest.completeInspection(InspectionResult.FAIL);
        returnRequest.fail(request.rejectReason());
        claim.reject(request.rejectReason());

        return new ReturnInspectResponse(
                returnRequest.getReturnRequestId(),
                returnRequest.getStatus(),
                InspectionResult.FAIL,
                BigDecimal.ZERO,
                LocalDateTime.now()
        );
    }

    private void updateOrderStatus(Order order) {
        boolean hasRemaining = order.getItems().stream()
                .anyMatch(i -> !isCanceled(i));
        order.cancel(hasRemaining);
    }

    private boolean isCanceled(OrderItem item) {
        return item.getStatus() == OrderItemStatus.CANCELED;
    }

    private void publishReturnCompletedEvent(
            Order order,
            ReturnRequest returnRequest,
            OrderItem orderItem,
            BigDecimal refundedAmount
    ) {
        try {
            String payload = objectMapper.writeValueAsString(
                    OrderReturnCompletedEvent.envelopeOf(order, returnRequest, orderItem, refundedAmount)
            );
            outboxRepository.save(OutboxEvent.create(
                    KafkaTopics.ORDER_RETURN_COMPLETED,
                    order.getOrderId().toString(),
                    payload
            ));
        } catch (Exception e) {
            log.error("OrderReturnCompletedEvent Outbox 저장 실패. orderId={}, returnRequestId={}",
                    order.getOrderId(), returnRequest.getReturnRequestId(), e);
        }
    }
}
