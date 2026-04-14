package com.example.payment.application.service;

import com.example.payment.application.dto.PaymentRefundCommand;
import com.example.payment.application.dto.PaymentRefundItemCommand;
import com.example.payment.application.dto.PaymentRefundItemResult;
import com.example.payment.application.dto.PaymentRefundResult;
import com.example.payment.application.usecase.PaymentRefundUseCase;
import com.example.payment.common.exception.InvalidOrderPaymentRequestException;
import com.example.payment.domain.entity.PaymentRefund;
import com.example.payment.domain.entity.PaymentRefundItem;
import com.example.payment.domain.repository.PaymentRefundItemRepository;
import com.example.payment.domain.repository.PaymentRefundRepository;
import com.example.payment.domain.service.IdentifierGenerator;
import com.example.payment.domain.service.TimeProvider;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentRefundService implements PaymentRefundUseCase {

    private final PaymentRefundRepository paymentRefundRepository;
    private final PaymentRefundItemRepository paymentRefundItemRepository;
    private final IdentifierGenerator identifierGenerator;
    private final TimeProvider timeProvider;

    public PaymentRefundService(
            PaymentRefundRepository paymentRefundRepository,
            PaymentRefundItemRepository paymentRefundItemRepository,
            IdentifierGenerator identifierGenerator,
            TimeProvider timeProvider
    ) {
        this.paymentRefundRepository = paymentRefundRepository;
        this.paymentRefundItemRepository = paymentRefundItemRepository;
        this.identifierGenerator = identifierGenerator;
        this.timeProvider = timeProvider;
    }

    @Override
    @Transactional
    public PaymentRefundResult requestRefund(PaymentRefundCommand command) {
        validateRefundCommand(command);

        PaymentRefund existingRefund = paymentRefundRepository.findByOrderCancelRequestId(command.orderCancelRequestId())
                .orElse(null);
        if (existingRefund != null) {
            return toPaymentRefundResult(existingRefund, paymentRefundItemRepository.findAllByRefundId(existingRefund.getRefundId()));
        }

        LocalDateTime requestedAt = timeProvider.now();
        Long totalRefundAmount = calculateTotalRefundAmount(command.items());

        PaymentRefund paymentRefund = PaymentRefund.createRequested(
                identifierGenerator.generateUuid(),
                command.orderCancelRequestId(),
                command.orderId(),
                command.buyerMemberId(),
                command.refundType(),
                command.paymentMethod(),
                totalRefundAmount,
                command.reason(),
                requestedAt
        );
        paymentRefundRepository.save(paymentRefund);

        List<PaymentRefundItem> paymentRefundItems = createRequestedRefundItems(paymentRefund.getRefundId(), command.items(), requestedAt);
        paymentRefundItemRepository.saveAll(paymentRefundItems);

        return toPaymentRefundResult(paymentRefund, paymentRefundItems);
    }

    private void validateRefundCommand(PaymentRefundCommand command) {
        if (command == null) {
            throw new InvalidOrderPaymentRequestException("payment refund command is required.");
        }
        if (command.orderId() == null) {
            throw new InvalidOrderPaymentRequestException("orderId is required.");
        }
        if (command.buyerMemberId() == null) {
            throw new InvalidOrderPaymentRequestException("buyerMemberId is required.");
        }
        if (command.orderCancelRequestId() == null) {
            throw new InvalidOrderPaymentRequestException("orderCancelRequestId is required.");
        }
        if (command.refundType() == null) {
            throw new InvalidOrderPaymentRequestException("refundType is required.");
        }
        if (command.paymentMethod() == null) {
            throw new InvalidOrderPaymentRequestException("paymentMethod is required.");
        }
        if (command.items() == null || command.items().isEmpty()) {
            throw new InvalidOrderPaymentRequestException("refund items must not be empty.");
        }
        for (PaymentRefundItemCommand item : command.items()) {
            validateRefundItemCommand(item);
        }
    }

    private void validateRefundItemCommand(PaymentRefundItemCommand item) {
        if (item == null) {
            throw new InvalidOrderPaymentRequestException("refund item must not be null.");
        }
        if (item.orderItemId() == null) {
            throw new InvalidOrderPaymentRequestException("orderItemId is required.");
        }
        if (item.refundAmount() == null || item.refundAmount() <= 0) {
            throw new InvalidOrderPaymentRequestException("refundAmount must be positive.");
        }
    }

    private Long calculateTotalRefundAmount(List<PaymentRefundItemCommand> items) {
        long totalAmount = 0L;
        for (PaymentRefundItemCommand item : items) {
            totalAmount += item.refundAmount();
        }
        if (totalAmount <= 0L) {
            throw new InvalidOrderPaymentRequestException("total refund amount must be positive.");
        }
        return totalAmount;
    }

    private List<PaymentRefundItem> createRequestedRefundItems(
            UUID refundId,
            List<PaymentRefundItemCommand> itemCommands,
            LocalDateTime requestedAt
    ) {
        return itemCommands.stream()
                .map(item -> PaymentRefundItem.createRequested(
                        identifierGenerator.generateUuid(),
                        refundId,
                        item.orderItemId(),
                        item.refundAmount(),
                        requestedAt
                ))
                .toList();
    }

    private PaymentRefundResult toPaymentRefundResult(PaymentRefund paymentRefund, List<PaymentRefundItem> paymentRefundItems) {
        List<PaymentRefundItemResult> itemResults = paymentRefundItems.stream()
                .map(item -> new PaymentRefundItemResult(
                        item.getOrderItemId(),
                        item.getStatus(),
                        item.getRefundAmount()
                ))
                .toList();

        LocalDateTime processedAt = paymentRefund.getCompletedAt() == null
                ? paymentRefund.getUpdatedAt()
                : paymentRefund.getCompletedAt();

        return new PaymentRefundResult(
                paymentRefund.getRefundId(),
                paymentRefund.getOrderId(),
                paymentRefund.getOrderCancelRequestId(),
                paymentRefund.getRefundStatus(),
                paymentRefund.getRefundType(),
                paymentRefund.getTotalRefundAmount(),
                itemResults,
                Objects.requireNonNull(processedAt)
        );
    }
}
