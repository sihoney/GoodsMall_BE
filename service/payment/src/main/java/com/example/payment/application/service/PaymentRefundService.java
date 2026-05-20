package com.example.payment.application.service;

import com.example.payment.application.dto.PaymentRefundCommand;
import com.example.payment.application.dto.PaymentRefundItemCommand;
import com.example.payment.application.dto.PaymentRefundItemResult;
import com.example.payment.application.dto.PaymentRefundResult;
import com.example.payment.application.dto.SellerRefundCommand;
import com.example.payment.application.usecase.PaymentCancellationUseCase;
import com.example.payment.application.usecase.SellerRefundUseCase;
import com.example.payment.common.exception.InvalidOrderPaymentRequestException;
import com.example.payment.common.exception.WalletNotFoundException;
import com.example.payment.domain.entity.CardTransaction;
import com.example.payment.domain.entity.Escrow;
import com.example.payment.domain.entity.EscrowTransaction;
import com.example.payment.domain.entity.OrderPayment;
import com.example.payment.domain.entity.PaymentRefund;
import com.example.payment.domain.entity.PaymentRefundAllocation;
import com.example.payment.domain.entity.PaymentRefundItem;
import com.example.payment.domain.entity.Wallet;
import com.example.payment.domain.entity.WalletTransaction;
import com.example.payment.domain.enumtype.CardTransactionCancelScope;
import com.example.payment.domain.enumtype.EscrowReferenceType;
import com.example.payment.domain.enumtype.PaymentRefundMethod;
import com.example.payment.domain.enumtype.PaymentRefundStatus;
import com.example.payment.domain.enumtype.PaymentRefundType;
import com.example.payment.domain.enumtype.WalletTransactionType;
import com.example.payment.domain.repository.CardTransactionRepository;
import com.example.payment.domain.repository.EscrowRepository;
import com.example.payment.domain.repository.EscrowTransactionRepository;
import com.example.payment.domain.repository.OrderPaymentRepository;
import com.example.payment.domain.repository.PaymentRefundAllocationRepository;
import com.example.payment.domain.repository.PaymentRefundItemRepository;
import com.example.payment.domain.repository.PaymentRefundRepository;
import com.example.payment.domain.repository.WalletRepository;
import com.example.payment.domain.repository.WalletTransactionRepository;
import com.example.payment.domain.service.IdentifierGenerator;
import com.example.payment.domain.service.OrderRefundNotificationGateway;
import com.example.payment.domain.service.TimeProvider;
import com.example.payment.domain.service.TossPaymentGateway;
import com.example.payment.infrastructure.messaging.kafka.OrderRefundResultOutboxEventSaver;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderRefundResultMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderRefundResultStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PaymentRefundService implements PaymentCancellationUseCase, SellerRefundUseCase {

    private static final BigDecimal ROUND_TRIP_DELIVERY_FEE = BigDecimal.valueOf(6_000L);

    private final PaymentRefundRepository paymentRefundRepository;
    private final PaymentRefundAllocationRepository paymentRefundAllocationRepository;
    private final PaymentRefundItemRepository paymentRefundItemRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final CardTransactionRepository cardTransactionRepository;
    private final EscrowRepository escrowRepository;
    private final EscrowTransactionRepository escrowTransactionRepository;
    private final TossPaymentGateway tossPaymentGateway;
    private final IdentifierGenerator identifierGenerator;
    private final TimeProvider timeProvider;
    private final OrderPaymentRepository orderPaymentRepository;
    private final OrderRefundNotificationGateway orderRefundNotificationGateway;
    private final OrderRefundResultOutboxEventSaver orderRefundResultOutboxEventSaver;

    public PaymentRefundService(
            PaymentRefundRepository paymentRefundRepository,
            PaymentRefundAllocationRepository paymentRefundAllocationRepository,
            PaymentRefundItemRepository paymentRefundItemRepository,
            WalletRepository walletRepository,
            WalletTransactionRepository walletTransactionRepository,
            CardTransactionRepository cardTransactionRepository,
            EscrowRepository escrowRepository,
            EscrowTransactionRepository escrowTransactionRepository,
            TossPaymentGateway tossPaymentGateway,
            IdentifierGenerator identifierGenerator,
            TimeProvider timeProvider,
            OrderPaymentRepository orderPaymentRepository,
            OrderRefundNotificationGateway orderRefundNotificationGateway,
            OrderRefundResultOutboxEventSaver orderRefundResultOutboxEventSaver
    ) {
        this.paymentRefundRepository = paymentRefundRepository;
        this.paymentRefundAllocationRepository = paymentRefundAllocationRepository;
        this.paymentRefundItemRepository = paymentRefundItemRepository;
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.cardTransactionRepository = cardTransactionRepository;
        this.escrowRepository = escrowRepository;
        this.escrowTransactionRepository = escrowTransactionRepository;
        this.tossPaymentGateway = tossPaymentGateway;
        this.identifierGenerator = identifierGenerator;
        this.timeProvider = timeProvider;
        this.orderPaymentRepository = orderPaymentRepository;
        this.orderRefundNotificationGateway = orderRefundNotificationGateway;
        this.orderRefundResultOutboxEventSaver = orderRefundResultOutboxEventSaver;
    }

    @Override
    @Transactional
    public PaymentRefundResult requestCancellation(PaymentRefundCommand command) {
        return processCancellation(command, CompletionFlow.CANCELLATION_API);
    }

    @Override
    @Transactional
    public PaymentRefundResult requestSellerRefund(SellerRefundCommand command) {
        PaymentRefundCommand sellerRefundCommand = buildSellerRefundCommandFromEscrow(command);
        return processCancellation(sellerRefundCommand, CompletionFlow.SELLER_REFUND_KAFKA);
    }

    private PaymentRefundResult processCancellation(
            PaymentRefundCommand command,
            CompletionFlow completionFlow
    ) {
        validateRefundCommand(command);

        PaymentRefund existingRefund = findExistingRefundByRequestId(command.orderCancelRequestId());
        if (existingRefund != null) {
            return toPaymentRefundResult(existingRefund, paymentRefundItemRepository.findAllByRefundId(existingRefund.getRefundId()));
        }

        // Serialize same-order refund attempts to avoid concurrent double processing.
        escrowRepository.lockAllByOrderId(command.orderId());

        existingRefund = findExistingRefundByRequestId(command.orderCancelRequestId());
        if (existingRefund != null) {
            return toPaymentRefundResult(existingRefund, paymentRefundItemRepository.findAllByRefundId(existingRefund.getRefundId()));
        }

        LocalDateTime refundRequestedAt = timeProvider.now();
        BigDecimal requestedTotalRefundAmount = calculateTotalRefundAmount(command.items());
        PaymentRefundMethod resolvedPaymentMethod = resolvePaymentRefundMethod(command.items());
        validateCardRefundReason(resolvedPaymentMethod, command.reason(), requestedTotalRefundAmount);

        PaymentRefund savedRequestedRefund = saveRequestedRefund(
                command,
                resolvedPaymentMethod,
                requestedTotalRefundAmount,
                refundRequestedAt
        );

        List<PaymentRefundItem> requestedRefundItems = createRequestedRefundItems(
                savedRequestedRefund.getRefundId(),
                command.items(),
                refundRequestedAt
        );
        List<PaymentRefundItem> savedRequestedRefundItems = paymentRefundItemRepository.saveAll(requestedRefundItems);

        LocalDateTime processingAt = timeProvider.now();
        savedRequestedRefund.markProcessing(processingAt);
        PaymentRefund processingRefund = paymentRefundRepository.save(savedRequestedRefund);

        try {
            if (processingRefund.getTotalRefundAmount().compareTo(BigDecimal.ZERO) > 0) {
                List<PaymentRefundAllocation> refundAllocations = executeRefundByPaymentMethod(processingRefund, command.items());
                paymentRefundAllocationRepository.saveAll(refundAllocations);
                refundEscrows(
                        processingRefund.getOrderId(),
                        processingRefund.getTotalRefundAmount(),
                        processingRefund.getBuyerMemberId(),
                        processingRefund.getRefundId(),
                        command.items(),
                        timeProvider.now()
                );
            }
            updateOrderPaymentStatusAfterRefundSucceeded(processingRefund.getOrderId());

            LocalDateTime succeededAt = timeProvider.now();
            markAllRefundItemsSucceeded(savedRequestedRefundItems, succeededAt);
            List<PaymentRefundItem> succeededRefundItems = paymentRefundItemRepository.saveAll(savedRequestedRefundItems);

            processingRefund.markSucceeded(succeededAt, succeededAt);
            PaymentRefund succeededRefund = paymentRefundRepository.save(processingRefund);

            notifyCompletionByFlow(succeededRefund, command.items(), completionFlow);

            return toPaymentRefundResult(succeededRefund, succeededRefundItems);
        } catch (RuntimeException exception) {
            LocalDateTime failedAt = timeProvider.now();
            String failureReason = resolveFailureReason(exception.getMessage());

            markAllRefundItemsFailed(savedRequestedRefundItems, failureReason, failedAt);
            List<PaymentRefundItem> failedRefundItems = paymentRefundItemRepository.saveAll(savedRequestedRefundItems);

            processingRefund.markFailed(failedAt, failedAt);
            PaymentRefund failedRefund = paymentRefundRepository.save(processingRefund);

            return toPaymentRefundResult(failedRefund, failedRefundItems);
        }
    }

    private PaymentRefundCommand buildSellerRefundCommandFromEscrow(SellerRefundCommand command) {
        validateSellerRefundCommand(command);

        List<Escrow> orderItemEscrows = escrowRepository.findAllByReferenceTypeAndReferenceIdIn(
                EscrowReferenceType.ORDER_ITEM,
                command.orderItemIds()
        );

        Map<UUID, Escrow> escrowByOrderItemId = new HashMap<>();
        for (Escrow escrow : orderItemEscrows) {
            escrowByOrderItemId.put(escrow.getReferenceId(), escrow);
        }

        UUID buyerMemberId = null;
        List<PaymentRefundItemCommand> refundItems = new ArrayList<>();
        for (UUID orderItemId : command.orderItemIds()) {
            Escrow escrow = escrowByOrderItemId.get(orderItemId);
            if (escrow == null) {
                throw new InvalidOrderPaymentRequestException("주문 항목에 대한 에스크로를 찾을 수 없습니다. orderItemId=" + orderItemId);
            }
            if (!Objects.equals(escrow.getOrderId(), command.orderId())) {
                throw new InvalidOrderPaymentRequestException("주문 항목의 에스크로와 주문 ID가 일치하지 않습니다. orderItemId=" + orderItemId);
            }
            if (!Objects.equals(escrow.getSellerMemberId(), command.sellerMemberId())) {
                throw new InvalidOrderPaymentRequestException("해당 주문 항목에 대한 판매자 권한이 없습니다. orderItemId=" + orderItemId);
            }
            if (!escrow.isHeld()) {
                throw new InvalidOrderPaymentRequestException("판매자 환불은 구매 확정 전 상태에서만 가능합니다.");
            }

            if (buyerMemberId == null) {
                buyerMemberId = escrow.getBuyerMemberId();
            } else if (!Objects.equals(buyerMemberId, escrow.getBuyerMemberId())) {
                throw new InvalidOrderPaymentRequestException("판매자 환불 항목들의 구매자 회원 ID는 모두 같아야 합니다.");
            }
            refundItems.add(new PaymentRefundItemCommand(orderItemId, escrow.getAmount()));
        }

        return new PaymentRefundCommand(
                command.orderId(),
                Objects.requireNonNull(buyerMemberId),
                command.orderCancelRequestId(),
                command.refundType(),
                command.reason(),
                applyRoundTripDeliveryFeeDeduction(refundItems)
        );
    }

    private void validateSellerRefundCommand(SellerRefundCommand command) {
        if (command == null) {
            throw new InvalidOrderPaymentRequestException("판매자 환불 요청은 필수입니다.");
        }
        if (command.orderId() == null) {
            throw new InvalidOrderPaymentRequestException("주문 ID는 필수입니다.");
        }
        if (command.sellerMemberId() == null) {
            throw new InvalidOrderPaymentRequestException("판매자 회원 ID는 필수입니다.");
        }
        if (command.orderCancelRequestId() == null) {
            throw new InvalidOrderPaymentRequestException("주문 취소 요청 ID는 필수입니다.");
        }
        if (command.refundType() == null) {
            throw new InvalidOrderPaymentRequestException("환불 유형은 필수입니다.");
        }
        if (command.orderItemIds() == null || command.orderItemIds().isEmpty()) {
            throw new InvalidOrderPaymentRequestException("주문 항목 ID 목록은 비어 있을 수 없습니다.");
        }

        Set<UUID> seenOrderItemIds = new HashSet<>();
        for (UUID orderItemId : command.orderItemIds()) {
            if (orderItemId == null) {
                throw new InvalidOrderPaymentRequestException("주문 항목 ID는 필수입니다.");
            }
            if (!seenOrderItemIds.add(orderItemId)) {
                throw new InvalidOrderPaymentRequestException("판매자 환불 항목의 주문 항목 ID는 중복될 수 없습니다.");
            }
        }
    }

    private List<PaymentRefundItemCommand> applyRoundTripDeliveryFeeDeduction(List<PaymentRefundItemCommand> items) {
        BigDecimal remainingFee = ROUND_TRIP_DELIVERY_FEE;
        List<PaymentRefundItemCommand> deductedItems = new ArrayList<>();
        for (PaymentRefundItemCommand item : items) {
            BigDecimal amount = item.refundAmount();
            BigDecimal deductedAmount;
            if (remainingFee.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal consumedFee = remainingFee.min(amount);
                deductedAmount = amount.subtract(consumedFee);
                remainingFee = remainingFee.subtract(consumedFee);
            } else {
                deductedAmount = amount;
            }
            deductedItems.add(new PaymentRefundItemCommand(item.orderItemId(), deductedAmount));
        }
        return deductedItems;
    }

    private void validateRefundCommand(PaymentRefundCommand command) {
        if (command == null) {
            throw new InvalidOrderPaymentRequestException("결제 환불 요청은 필수입니다.");
        }
        if (command.orderId() == null) {
            throw new InvalidOrderPaymentRequestException("주문 ID는 필수입니다.");
        }
        if (command.buyerMemberId() == null) {
            throw new InvalidOrderPaymentRequestException("구매자 회원 ID는 필수입니다.");
        }
        if (command.orderCancelRequestId() == null) {
            throw new InvalidOrderPaymentRequestException("주문 취소 요청 ID는 필수입니다.");
        }
        if (command.refundType() == null) {
            throw new InvalidOrderPaymentRequestException("환불 유형은 필수입니다.");
        }
        if (command.items() == null || command.items().isEmpty()) {
            throw new InvalidOrderPaymentRequestException("환불 항목은 비어 있을 수 없습니다.");
        }

        Set<UUID> seenOrderItemIds = new HashSet<>();
        for (PaymentRefundItemCommand item : command.items()) {
            validateRefundItemCommand(item);
            if (!seenOrderItemIds.add(item.orderItemId())) {
                throw new InvalidOrderPaymentRequestException("환불 항목의 주문 항목 ID는 중복될 수 없습니다.");
            }
        }
    }

    private PaymentRefund findExistingRefundByRequestId(UUID orderCancelRequestId) {
        return paymentRefundRepository.findByOrderCancelRequestId(orderCancelRequestId).orElse(null);
    }

    private PaymentRefund saveRequestedRefund(
            PaymentRefundCommand command,
            PaymentRefundMethod paymentRefundMethod,
            BigDecimal requestedTotalRefundAmount,
            LocalDateTime refundRequestedAt
    ) {
        PaymentRefund requestedRefund = PaymentRefund.createRequested(
                identifierGenerator.generateUuid(),
                command.orderCancelRequestId(),
                command.orderId(),
                command.buyerMemberId(),
                command.refundType(),
                paymentRefundMethod,
                requestedTotalRefundAmount,
                command.reason(),
                refundRequestedAt
        );

        try {
            return paymentRefundRepository.save(requestedRefund);
        } catch (DataIntegrityViolationException exception) {
            PaymentRefund existingRefund = findExistingRefundByRequestId(command.orderCancelRequestId());
            if (existingRefund != null) {
                return existingRefund;
            }
            throw exception;
        }
    }

    private void validateRefundItemCommand(PaymentRefundItemCommand item) {
        if (item == null) {
            throw new InvalidOrderPaymentRequestException("환불 항목에 비어 있는 값이 포함될 수 없습니다.");
        }
        if (item.orderItemId() == null) {
            throw new InvalidOrderPaymentRequestException("주문 항목 ID는 필수입니다.");
        }
        if (item.refundAmount() == null || item.refundAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidOrderPaymentRequestException("환불 금액은 음수일 수 없습니다.");
        }
    }

    private BigDecimal calculateTotalRefundAmount(List<PaymentRefundItemCommand> items) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PaymentRefundItemCommand item : items) {
            totalAmount = totalAmount.add(item.refundAmount());
        }
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidOrderPaymentRequestException("총 환불 금액은 음수일 수 없습니다.");
        }
        return totalAmount;
    }

    private PaymentRefundMethod resolvePaymentRefundMethod(List<PaymentRefundItemCommand> itemCommands) {
        List<CardTransaction> originalCardPayments = findOriginalCardPayments(itemCommands);
        if (originalCardPayments.isEmpty()) {
            return PaymentRefundMethod.WALLET;
        }

        int requestedOrderItemCount = itemCommands.stream()
                .map(PaymentRefundItemCommand::orderItemId)
                .collect(java.util.stream.Collectors.toSet())
                .size();

        int cardPaidOrderItemCount = originalCardPayments.stream()
                .map(CardTransaction::getReferenceId)
                .collect(java.util.stream.Collectors.toSet())
                .size();

        if (cardPaidOrderItemCount == requestedOrderItemCount) {
            return PaymentRefundMethod.CARD;
        }

        return PaymentRefundMethod.MIXED;
    }

    private void validateCardRefundReason(
            PaymentRefundMethod paymentRefundMethod,
            String refundReason,
            BigDecimal totalRefundAmount
    ) {
        if (totalRefundAmount == null || totalRefundAmount.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        if ((paymentRefundMethod == PaymentRefundMethod.CARD || paymentRefundMethod == PaymentRefundMethod.MIXED)
                && (refundReason == null || refundReason.isBlank())) {
            throw new InvalidOrderPaymentRequestException("카드 환불 사유는 필수입니다.");
        }
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

    private List<PaymentRefundAllocation> executeRefundByPaymentMethod(
            PaymentRefund paymentRefund,
            List<PaymentRefundItemCommand> itemCommands
    ) {
        LocalDateTime now = timeProvider.now();
        if (paymentRefund.getPaymentMethod() == PaymentRefundMethod.WALLET) {
            return List.of(executeWalletRefund(paymentRefund, paymentRefund.getTotalRefundAmount(), now));
        }
        if (paymentRefund.getPaymentMethod() == PaymentRefundMethod.CARD) {
            return List.of(executeCardRefund(paymentRefund, itemCommands));
        }
        if (paymentRefund.getPaymentMethod() == PaymentRefundMethod.MIXED) {
            return executeMixedRefund(paymentRefund, itemCommands, now);
        }
        throw new InvalidOrderPaymentRequestException("지원하지 않는 결제 수단입니다.");
    }

    private PaymentRefundAllocation executeWalletRefund(
            PaymentRefund paymentRefund,
            BigDecimal refundAmount,
            LocalDateTime refundedAt
    ) {
        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOrderPaymentRequestException("지갑 환불 금액은 0보다 커야 합니다.");
        }
        Wallet buyerWallet = walletRepository.findByMemberId(paymentRefund.getBuyerMemberId())
                .orElseThrow(WalletNotFoundException::new);

        BigDecimal balanceAfter = buyerWallet.increaseBalance(refundAmount, refundedAt);
        walletRepository.save(buyerWallet);

        WalletTransaction refundTransaction = WalletTransaction.create(
                identifierGenerator.generateUuid(),
                buyerWallet.getWalletId(),
                refundAmount,
                balanceAfter,
                WalletTransactionType.REFUND,
                paymentRefund.getRefundId(),
                "REFUND",
                "order refund",
                refundedAt
        );
        walletTransactionRepository.save(refundTransaction);

        return PaymentRefundAllocation.walletAllocation(
                identifierGenerator.generateUuid(),
                paymentRefund.getRefundId(),
                refundAmount,
                refundTransaction.getTransactionId(),
                refundedAt
        );
    }

    private void refundEscrows(
            UUID orderId,
            BigDecimal totalRefundAmount,
            UUID buyerMemberId,
            UUID refundId,
            List<PaymentRefundItemCommand> itemCommands,
            LocalDateTime refundedAt
    ) {
        Map<UUID, BigDecimal> requestedRefundAmountByOrderItemId = toRequestedRefundAmountByOrderItemId(itemCommands);

        List<Escrow> refundableEscrows = escrowRepository.findAllByReferenceTypeAndReferenceIdIn(
                EscrowReferenceType.ORDER_ITEM,
                requestedRefundAmountByOrderItemId.keySet().stream().toList()
        ).stream()
                .filter(Escrow::isHeld)
                .toList();

        if (refundableEscrows.isEmpty()) {
            throw new InvalidOrderPaymentRequestException("환불할 보관 상태 에스크로를 찾을 수 없습니다.");
        }

        Map<UUID, Escrow> escrowByOrderItemId = mapRefundableEscrowsByOrderItemId(refundableEscrows, buyerMemberId, orderId);
        BigDecimal appliedTotalRefundAmount = BigDecimal.ZERO;
        for (Map.Entry<UUID, BigDecimal> requestedRefundAmountEntry : requestedRefundAmountByOrderItemId.entrySet()) {
            Escrow escrow = escrowByOrderItemId.get(requestedRefundAmountEntry.getKey());
            if (escrow == null) {
                throw new InvalidOrderPaymentRequestException(
                        "주문 항목에 대한 보관 상태 에스크로를 찾을 수 없습니다. orderItemId=" + requestedRefundAmountEntry.getKey()
                );
            }

            BigDecimal beforeAmount = escrow.getAmount();
            escrow.applyRefundAmount(requestedRefundAmountEntry.getValue(), refundedAt, refundedAt);
            escrowRepository.save(escrow);
            recordRefundEscrowTransaction(
                    escrow,
                    requestedRefundAmountEntry.getValue(),
                    beforeAmount,
                    escrow.getAmount(),
                    refundId,
                    refundedAt
            );
            appliedTotalRefundAmount = appliedTotalRefundAmount.add(requestedRefundAmountEntry.getValue());
        }

        if (appliedTotalRefundAmount.compareTo(totalRefundAmount) != 0) {
            throw new InvalidOrderPaymentRequestException("에스크로 환불 금액 합계와 총 환불 금액이 일치하지 않습니다.");
        }
    }

    private void recordRefundEscrowTransaction(
            Escrow escrow,
            BigDecimal refundAmount,
            BigDecimal beforeAmount,
            BigDecimal afterAmount,
            UUID refundId,
            LocalDateTime occurredAt
    ) {
        EscrowTransaction transaction = EscrowTransaction.refund(
                identifierGenerator.generateUuid(),
                escrow.getEscrowId(),
                escrow.getOrderId(),
                escrow.isOrderItemReference() ? escrow.getReferenceId() : null,
                escrow.getSellerMemberId(),
                escrow.getBuyerMemberId(),
                refundAmount,
                beforeAmount,
                afterAmount,
                refundId,
                "PAYMENT_REFUND",
                "에스크로 환불",
                occurredAt,
                occurredAt
        );
        escrowTransactionRepository.save(transaction);
    }

    private Map<UUID, BigDecimal> toRequestedRefundAmountByOrderItemId(List<PaymentRefundItemCommand> itemCommands) {
        Map<UUID, BigDecimal> requestedRefundAmountByOrderItemId = new HashMap<>();
        for (PaymentRefundItemCommand itemCommand : itemCommands) {
            requestedRefundAmountByOrderItemId.merge(itemCommand.orderItemId(), itemCommand.refundAmount(), BigDecimal::add);
        }
        return requestedRefundAmountByOrderItemId;
    }

    private Map<UUID, Escrow> mapRefundableEscrowsByOrderItemId(List<Escrow> refundableEscrows, UUID buyerMemberId, UUID orderId) {
        Map<UUID, Escrow> refundableEscrowByOrderItemId = new HashMap<>();
        for (Escrow refundableEscrow : refundableEscrows) {
            if (!refundableEscrow.isOrderItemReference()) {
                continue;
            }
            if (!Objects.equals(refundableEscrow.getBuyerMemberId(), buyerMemberId)) {
                throw new InvalidOrderPaymentRequestException("에스크로의 구매자 회원 ID가 요청과 일치하지 않습니다.");
            }
            if (!Objects.equals(refundableEscrow.getOrderId(), orderId)) {
                throw new InvalidOrderPaymentRequestException("에스크로의 주문 ID가 요청과 일치하지 않습니다.");
            }
            refundableEscrowByOrderItemId.put(refundableEscrow.getReferenceId(), refundableEscrow);
        }
        return refundableEscrowByOrderItemId;
    }

    private PaymentRefundAllocation executeCardRefund(PaymentRefund paymentRefund, List<PaymentRefundItemCommand> itemCommands) {
        List<CardTransaction> originalPayments = findOriginalCardPayments(itemCommands);
        Map<UUID, CardTransaction> originalPaymentMap = mapOriginalPaymentsByOrderItemId(originalPayments);
        List<UUID> orderItemIds = itemCommands.stream().map(PaymentRefundItemCommand::orderItemId).distinct().toList();
        BigDecimal cardRefundAmount = calculateTotalRefundAmount(itemCommands);

        validateOriginalCardPayments(orderItemIds, originalPaymentMap, paymentRefund.getBuyerMemberId());
        UUID cardCancelTransactionGroupId = executeCardCancellation(
                paymentRefund,
                itemCommands,
                originalPayments,
                originalPaymentMap,
                cardRefundAmount
        );

        return PaymentRefundAllocation.cardAllocation(
                identifierGenerator.generateUuid(),
                paymentRefund.getRefundId(),
                cardRefundAmount,
                cardCancelTransactionGroupId,
                timeProvider.now()
        );
    }

    private List<PaymentRefundAllocation> executeMixedRefund(
            PaymentRefund paymentRefund,
            List<PaymentRefundItemCommand> itemCommands,
            LocalDateTime refundedAt
    ) {
        List<CardTransaction> originalPayments = findOriginalCardPayments(itemCommands);
        Map<UUID, CardTransaction> originalPaymentMap = mapOriginalPaymentsByOrderItemId(originalPayments);

        List<PaymentRefundItemCommand> cardRefundItems = new ArrayList<>();
        List<PaymentRefundItemCommand> walletRefundItems = new ArrayList<>();
        for (PaymentRefundItemCommand itemCommand : itemCommands) {
            if (originalPaymentMap.containsKey(itemCommand.orderItemId())) {
                cardRefundItems.add(itemCommand);
            } else {
                walletRefundItems.add(itemCommand);
            }
        }

        if (cardRefundItems.isEmpty() || walletRefundItems.isEmpty()) {
            throw new InvalidOrderPaymentRequestException("복합 환불은 카드 환불 항목과 지갑 환불 항목이 모두 필요합니다.");
        }

        List<UUID> cardOrderItemIds = cardRefundItems.stream()
                .map(PaymentRefundItemCommand::orderItemId)
                .distinct()
                .toList();
        validateOriginalCardPayments(cardOrderItemIds, originalPaymentMap, paymentRefund.getBuyerMemberId());

        BigDecimal cardRefundAmount = calculateTotalRefundAmount(cardRefundItems);
        UUID cardCancelTransactionGroupId = executeCardCancellation(
                paymentRefund,
                cardRefundItems,
                originalPayments,
                originalPaymentMap,
                cardRefundAmount
        );

        BigDecimal walletRefundAmount = calculateTotalRefundAmount(walletRefundItems);
        PaymentRefundAllocation walletAllocation = executeWalletRefund(paymentRefund, walletRefundAmount, refundedAt);

        PaymentRefundAllocation cardAllocation = PaymentRefundAllocation.cardAllocation(
                identifierGenerator.generateUuid(),
                paymentRefund.getRefundId(),
                cardRefundAmount,
                cardCancelTransactionGroupId,
                refundedAt
        );
        return List.of(cardAllocation, walletAllocation);
    }

    private List<CardTransaction> findOriginalCardPayments(List<PaymentRefundItemCommand> itemCommands) {
        List<UUID> orderItemIds = itemCommands.stream()
                .map(PaymentRefundItemCommand::orderItemId)
                .distinct()
                .toList();
        return cardTransactionRepository.findSuccessfulPaymentsByOrderItemIds(orderItemIds);
    }

    private UUID executeCardCancellation(
            PaymentRefund paymentRefund,
            List<PaymentRefundItemCommand> cardRefundItems,
            List<CardTransaction> originalPayments,
            Map<UUID, CardTransaction> originalPaymentMap,
            BigDecimal cancellationAmount
    ) {
        Map<UUID, BigDecimal> remainingAmountByOrderItemId = validateAndResolveRemainingAmounts(
                cardRefundItems,
                originalPaymentMap
        );

        String paymentKey = resolveCardPaymentKey(originalPayments);
        TossPaymentGateway.TossPaymentCancellation cancellation = tossPaymentGateway.cancel(
                paymentKey,
                paymentRefund.getRefundReason(),
                cancellationAmount
        );

        validateCardCancellationAmount(cancellationAmount, cancellation.canceledAmount());

        LocalDateTime canceledAt = cancellation.canceledAt();
        UUID cancelTransactionGroupId = identifierGenerator.generateUuid();
        CardTransactionCancelScope cancelScope = paymentRefund.getRefundType() == PaymentRefundType.FULL
                ? CardTransactionCancelScope.FULL
                : CardTransactionCancelScope.PARTIAL;

        List<CardTransaction> cancelTransactions = cardRefundItems.stream()
                .map(itemCommand -> createApprovedCancelTransaction(
                        cancelTransactionGroupId,
                        originalPaymentMap.get(itemCommand.orderItemId()),
                        itemCommand.refundAmount(),
                        remainingAmountByOrderItemId.get(itemCommand.orderItemId()),
                        cancelScope,
                        paymentRefund.getRefundReason(),
                        canceledAt
                ))
                .toList();
        cardTransactionRepository.saveAll(cancelTransactions);
        return cancelTransactionGroupId;
    }

    private Map<UUID, BigDecimal> validateAndResolveRemainingAmounts(
            List<PaymentRefundItemCommand> cardRefundItems,
            Map<UUID, CardTransaction> originalPaymentMap
    ) {
        List<UUID> originalTransactionIds = originalPaymentMap.values().stream()
                .map(CardTransaction::getCardTransactionId)
                .distinct()
                .toList();

        List<CardTransaction> successfulCancels = cardTransactionRepository.findSuccessfulCancelsByRelatedTransactionIds(
                originalTransactionIds
        );
        Map<UUID, BigDecimal> remainingAmountByOriginalTransactionId = resolveRemainingAmountByOriginalTransactionId(
                originalPaymentMap,
                successfulCancels
        );

        Map<UUID, BigDecimal> remainingAmountByOrderItemId = new HashMap<>();
        for (PaymentRefundItemCommand cardRefundItem : cardRefundItems) {
            CardTransaction originalPayment = Objects.requireNonNull(originalPaymentMap.get(cardRefundItem.orderItemId()));
            UUID originalTransactionId = originalPayment.getCardTransactionId();
            BigDecimal currentRemainingAmount = remainingAmountByOriginalTransactionId.get(originalTransactionId);
            if (currentRemainingAmount == null) {
                throw new InvalidOrderPaymentRequestException("주문 항목의 남은 카드 결제 금액을 찾을 수 없습니다. orderItemId=" + cardRefundItem.orderItemId());
            }
            if (cardRefundItem.refundAmount().compareTo(currentRemainingAmount) > 0) {
                throw new InvalidOrderPaymentRequestException("카드 환불 금액이 남은 결제 금액을 초과합니다. orderItemId=" + cardRefundItem.orderItemId());
            }

            BigDecimal nextRemainingAmount = currentRemainingAmount.subtract(cardRefundItem.refundAmount());
            remainingAmountByOriginalTransactionId.put(originalTransactionId, nextRemainingAmount);
            remainingAmountByOrderItemId.put(cardRefundItem.orderItemId(), nextRemainingAmount);
        }
        return remainingAmountByOrderItemId;
    }

    private Map<UUID, BigDecimal> resolveRemainingAmountByOriginalTransactionId(
            Map<UUID, CardTransaction> originalPaymentMap,
            List<CardTransaction> successfulCancels
    ) {
        Map<UUID, BigDecimal> canceledAmountByOriginalTransactionId = new HashMap<>();
        for (CardTransaction successfulCancel : successfulCancels) {
            UUID originalTransactionId = successfulCancel.getRelatedTransactionId();
            if (originalTransactionId == null) {
                continue;
            }
            canceledAmountByOriginalTransactionId.merge(
                    originalTransactionId,
                    resolveApprovedAmount(successfulCancel),
                    BigDecimal::add
            );
        }

        Map<UUID, BigDecimal> remainingAmountByOriginalTransactionId = new HashMap<>();
        for (CardTransaction originalPayment : originalPaymentMap.values()) {
            BigDecimal originalApprovedAmount = resolveApprovedAmount(originalPayment);
            BigDecimal canceledAmount = canceledAmountByOriginalTransactionId.getOrDefault(
                    originalPayment.getCardTransactionId(),
                    BigDecimal.ZERO
            );
            BigDecimal remainingAmount = originalApprovedAmount.subtract(canceledAmount);
            if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidOrderPaymentRequestException("남은 카드 결제 금액이 올바르지 않습니다. orderItemId=" + originalPayment.getReferenceId());
            }
            remainingAmountByOriginalTransactionId.put(originalPayment.getCardTransactionId(), remainingAmount);
        }
        return remainingAmountByOriginalTransactionId;
    }

    private BigDecimal resolveApprovedAmount(CardTransaction cardTransaction) {
        BigDecimal approvedAmount = cardTransaction.getApprovedAmount();
        if (approvedAmount != null && approvedAmount.compareTo(BigDecimal.ZERO) > 0) {
            return approvedAmount;
        }
        BigDecimal requestedAmount = cardTransaction.getRequestedAmount();
        if (requestedAmount != null && requestedAmount.compareTo(BigDecimal.ZERO) > 0) {
            return requestedAmount;
        }
        throw new InvalidOrderPaymentRequestException("카드 거래 금액이 올바르지 않습니다.");
    }

    private Map<UUID, CardTransaction> mapOriginalPaymentsByOrderItemId(List<CardTransaction> originalPayments) {
        Map<UUID, CardTransaction> paymentMap = new HashMap<>();
        for (CardTransaction originalPayment : originalPayments) {
            paymentMap.put(originalPayment.getReferenceId(), originalPayment);
        }
        return paymentMap;
    }

    private void validateOriginalCardPayments(
            List<UUID> orderItemIds,
            Map<UUID, CardTransaction> originalPaymentMap,
            UUID buyerMemberId
    ) {
        for (UUID orderItemId : orderItemIds) {
            CardTransaction originalPayment = originalPaymentMap.get(orderItemId);
            if (originalPayment == null) {
                throw new InvalidOrderPaymentRequestException("원본 카드 결제 내역을 찾을 수 없습니다. orderItemId=" + orderItemId);
            }
            if (!Objects.equals(originalPayment.getBuyerMemberId(), buyerMemberId)) {
                throw new InvalidOrderPaymentRequestException("원본 카드 결제의 구매자 회원 ID가 요청과 일치하지 않습니다.");
            }
        }
    }

    private String resolveCardPaymentKey(List<CardTransaction> originalPayments) {
        if (originalPayments.isEmpty()) {
            throw new InvalidOrderPaymentRequestException("원본 카드 결제 내역이 필요합니다.");
        }
        String paymentKey = originalPayments.get(0).getPgPaymentKey();
        if (paymentKey == null || paymentKey.isBlank()) {
            throw new InvalidOrderPaymentRequestException("원본 카드 결제의 paymentKey가 없습니다.");
        }
        for (CardTransaction originalPayment : originalPayments) {
            if (!Objects.equals(paymentKey, originalPayment.getPgPaymentKey())) {
                throw new InvalidOrderPaymentRequestException("환불 요청에 서로 다른 paymentKey가 포함되어 있습니다.");
            }
        }
        return paymentKey;
    }

    private void validateCardCancellationAmount(BigDecimal expectedAmount, BigDecimal canceledAmount) {
        if (expectedAmount.compareTo(canceledAmount) != 0) {
            throw new InvalidOrderPaymentRequestException("카드 취소 금액이 요청 금액과 일치하지 않습니다.");
        }
    }

    private CardTransaction createApprovedCancelTransaction(
            UUID cancelTransactionGroupId,
            CardTransaction originalPayment,
            BigDecimal cancelAmount,
            BigDecimal remainingAmount,
            CardTransactionCancelScope cancelScope,
            String cancelReason,
            LocalDateTime canceledAt
    ) {
        CardTransaction cancelTransaction = CardTransaction.pendingCancel(
                identifierGenerator.generateUuid(),
                cancelTransactionGroupId,
                originalPayment.getCardTransactionId(),
                originalPayment.getReferenceId(),
                originalPayment.getBuyerMemberId(),
                originalPayment.getPgOrderId(),
                originalPayment.getPgPaymentKey(),
                cancelScope,
                cancelAmount,
                cancelReason,
                canceledAt
        );
        cancelTransaction.approve(
                Objects.requireNonNull(originalPayment.getPgPaymentKey()),
                cancelAmount,
                remainingAmount,
                canceledAt
        );
        return cancelTransaction;
    }

    private void markAllRefundItemsSucceeded(List<PaymentRefundItem> paymentRefundItems, LocalDateTime updatedAt) {
        for (PaymentRefundItem paymentRefundItem : paymentRefundItems) {
            paymentRefundItem.markSucceeded(updatedAt);
        }
    }

    private void markAllRefundItemsFailed(List<PaymentRefundItem> paymentRefundItems, String failureReason, LocalDateTime updatedAt) {
        for (PaymentRefundItem paymentRefundItem : paymentRefundItems) {
            paymentRefundItem.markFailed(failureReason, updatedAt);
        }
    }

    private void updateOrderPaymentStatusAfterRefundSucceeded(UUID orderId) {
        OrderPayment orderPayment = orderPaymentRepository.findByOrderId(orderId).orElse(null);
        if (orderPayment == null) {
            return;
        }

        BigDecimal totalRefundedAmount = paymentRefundRepository.findAllByOrderIdAndRefundStatus(orderId, PaymentRefundStatus.SUCCEEDED)
                .stream()
                .map(PaymentRefund::getTotalRefundAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime now = timeProvider.now();
        orderPayment.markRefundStatusByTotalRefundedAmount(totalRefundedAmount, now);
        orderPaymentRepository.save(orderPayment);
    }

    private void notifyCompletionByFlow(
            PaymentRefund paymentRefund,
            List<PaymentRefundItemCommand> itemCommands,
            CompletionFlow completionFlow
    ) {
        if (completionFlow == CompletionFlow.CANCELLATION_API) {
            notifyOrderCancellationCompletedByApi(paymentRefund.getOrderId(), itemCommands);
            return;
        }
        publishOrderRefundCompletedByKafka(paymentRefund, itemCommands);
    }

    private void notifyOrderCancellationCompletedByApi(UUID orderId, List<PaymentRefundItemCommand> itemCommands) {
        List<UUID> orderItemIds = itemCommands.stream()
                .map(PaymentRefundItemCommand::orderItemId)
                .distinct()
                .toList();

        boolean notified = orderRefundNotificationGateway.notifyRefundCompleted(orderId, orderItemIds);
        if (!notified) {
            log.warn("Order refund completion notification failed. orderId={} orderItemCount={}", orderId, orderItemIds.size());
        }
    }

    private void publishOrderRefundCompletedByKafka(PaymentRefund paymentRefund, List<PaymentRefundItemCommand> itemCommands) {
        List<UUID> orderItemIds = itemCommands.stream()
                .map(PaymentRefundItemCommand::orderItemId)
                .distinct()
                .toList();
        orderRefundResultOutboxEventSaver.save(
                new OrderRefundResultMessage(
                        identifierGenerator.generateUuid(),
                        paymentRefund.getRefundId(),
                        paymentRefund.getOrderId(),
                        orderItemIds,
                        OrderRefundResultStatus.SUCCESS,
                        null,
                        Instant.now()
                )
        );
    }

    private enum CompletionFlow {
        CANCELLATION_API,
        SELLER_REFUND_KAFKA
    }

    private String resolveFailureReason(String failureReason) {
        if (failureReason == null || failureReason.isBlank()) {
            return "refund processing failed";
        }
        return failureReason;
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
