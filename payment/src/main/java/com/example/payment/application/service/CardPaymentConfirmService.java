package com.example.payment.application.service;

import com.example.payment.application.dto.CardPaymentConfirmCommand;
import com.example.payment.application.dto.CardPaymentConfirmResult;
import com.example.payment.application.dto.OrderPaymentValidationCommand;
import com.example.payment.application.usecase.CardPaymentConfirmUseCase;
import com.example.payment.application.usecase.OrderPaymentValidationUseCase;
import com.example.payment.common.exception.InvalidCardPaymentRequestException;
import com.example.payment.common.exception.PaymentGatewayException;
import com.example.payment.domain.entity.CardTransaction;
import com.example.payment.domain.entity.Escrow;
import com.example.payment.domain.entity.OrderPayment;
import com.example.payment.domain.entity.OrderPaymentAllocation;
import com.example.payment.domain.enumtype.EscrowReferenceType;
import com.example.payment.domain.enumtype.OrderPaymentMethod;
import com.example.payment.domain.enumtype.CardTransactionStatus;
import com.example.payment.domain.repository.OrderPaymentAllocationRepository;
import com.example.payment.domain.repository.OrderPaymentRepository;
import com.example.payment.domain.repository.CardTransactionRepository;
import com.example.payment.domain.repository.EscrowRepository;
import com.example.payment.domain.service.CardConfirmResultEventPublisher;
import com.example.payment.domain.service.IdentifierGenerator;
import com.example.payment.domain.service.OrderPaymentValidationData;
import com.example.payment.domain.service.OrderPaymentValidationItemData;
import com.example.payment.domain.service.TimeProvider;
import com.example.payment.domain.service.TossPaymentGateway;
import com.example.payment.infrastructure.messaging.kafka.contract.CardConfirmResultMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.CardConfirmResultStatus;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CardPaymentConfirmService implements CardPaymentConfirmUseCase {

    private static final String CARD_METHOD = "카드";
    private static final String EASY_PAY_METHOD = "간편결제";

    private final CardTransactionRepository cardTransactionRepository;
    private final EscrowRepository escrowRepository;
    private final OrderPaymentRepository orderPaymentRepository;
    private final OrderPaymentAllocationRepository orderPaymentAllocationRepository;
    private final TossPaymentGateway tossPaymentGateway;
    private final OrderPaymentValidationUseCase orderPaymentValidationUseCase;
    private final CardConfirmResultEventPublisher cardConfirmResultEventPublisher;
    private final IdentifierGenerator identifierGenerator;
    private final TimeProvider timeProvider;

    public CardPaymentConfirmService(
            CardTransactionRepository cardTransactionRepository,
            EscrowRepository escrowRepository,
            OrderPaymentRepository orderPaymentRepository,
            OrderPaymentAllocationRepository orderPaymentAllocationRepository,
            TossPaymentGateway tossPaymentGateway,
            OrderPaymentValidationUseCase orderPaymentValidationUseCase,
            CardConfirmResultEventPublisher cardConfirmResultEventPublisher,
            IdentifierGenerator identifierGenerator,
            TimeProvider timeProvider
    ) {
        this.cardTransactionRepository = cardTransactionRepository;
        this.escrowRepository = escrowRepository;
        this.orderPaymentRepository = orderPaymentRepository;
        this.orderPaymentAllocationRepository = orderPaymentAllocationRepository;
        this.tossPaymentGateway = tossPaymentGateway;
        this.orderPaymentValidationUseCase = orderPaymentValidationUseCase;
        this.cardConfirmResultEventPublisher = cardConfirmResultEventPublisher;
        this.identifierGenerator = identifierGenerator;
        this.timeProvider = timeProvider;
    }

    @Override
    public CardPaymentConfirmResult confirmCardPayment(CardPaymentConfirmCommand command) {
        try {
            validateCommand(command);
            OrderPaymentValidationData validationData = validateOrderPayment(command);

            UUID transactionGroupId = identifierGenerator.generateUuid();
            var requestedAt = timeProvider.now();
            List<CardTransaction> cardTransactions = createPendingTransactions(
                    command,
                    validationData.orderItems(),
                    transactionGroupId,
                    requestedAt
            );

            cardTransactionRepository.saveAll(cardTransactions);

            TossPaymentGateway.TossPaymentConfirmation confirmation = confirmWithToss(command, cardTransactions);
            validateConfirmation(command, confirmation);

            for (CardTransaction cardTransaction : cardTransactions) {
                cardTransaction.approve(
                        confirmation.paymentKey(),
                        cardTransaction.getRequestedAmount(),
                        java.math.BigDecimal.ZERO,
                        confirmation.approvedAt()
                );
            }

            cardTransactionRepository.saveAll(cardTransactions);
            List<Escrow> heldEscrows = createHeldEscrows(
                    command,
                    validationData.orderItems(),
                    confirmation.approvedAt()
            );
            escrowRepository.saveAll(heldEscrows);
            saveOrderPaymentRecords(command, transactionGroupId, confirmation.approvedAt());
            publishCardConfirmSuccess(command.orderId());

            return new CardPaymentConfirmResult(
                    transactionGroupId,
                    command.orderId(),
                    command.buyerId(),
                    confirmation.approvedAmount(),
                    CardTransactionStatus.SUCCESS,
                    confirmation.approvedAt()
            );
        } catch (RuntimeException exception) {
            if (command != null && command.orderId() != null) {
                publishCardConfirmFailure(command.orderId(), resolveFailReason(exception));
            }
            throw exception;
        }
    }

    private void validateCommand(CardPaymentConfirmCommand command) {
        if (command == null) {
            throw new InvalidCardPaymentRequestException("카드 결제 승인 요청이 필요합니다.");
        }
        if (command.buyerId() == null) {
            throw new InvalidCardPaymentRequestException("구매자 ID는 필수입니다.");
        }
        if (command.orderId() == null) {
            throw new InvalidCardPaymentRequestException("주문 ID는 필수입니다.");
        }
        if (command.paymentKey() == null || command.paymentKey().isBlank()) {
            throw new InvalidCardPaymentRequestException("paymentKey는 필수입니다.");
        }
        if (command.amount() == null || command.amount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new InvalidCardPaymentRequestException("결제 금액은 0보다 커야 합니다.");
        }

    }

    private OrderPaymentValidationData validateOrderPayment(CardPaymentConfirmCommand command) {
        OrderPaymentValidationData validationData = orderPaymentValidationUseCase.validateOrderPayment(
                new OrderPaymentValidationCommand(command.orderId(), command.buyerId(), command.amount())
        );
        if (validationData == null || validationData.orderItems() == null || validationData.orderItems().isEmpty()) {
            throw new InvalidCardPaymentRequestException("주문 결제 검증 결과에 주문 항목이 없습니다.");
        }

        java.math.BigDecimal orderItemsTotalAmount = getOrderItemsTotalAmount(validationData);

        if (orderItemsTotalAmount.compareTo(command.amount()) != 0) {
            throw new InvalidCardPaymentRequestException("주문 항목 합계와 요청 결제 금액이 일치하지 않습니다.");
        }
        return validationData;
    }

    private static java.math.BigDecimal getOrderItemsTotalAmount(OrderPaymentValidationData validationData) {
        java.math.BigDecimal orderItemsTotalAmount = java.math.BigDecimal.ZERO;
        for (OrderPaymentValidationItemData orderItem : validationData.orderItems()) {
            if (orderItem == null) {
                throw new InvalidCardPaymentRequestException("주문 결제 검증 결과에 비어 있는 주문 항목이 포함되어 있습니다.");
            }
            if (orderItem.orderItemId() == null) {
                throw new InvalidCardPaymentRequestException("주문 결제 검증 결과에 주문 항목 ID가 없습니다.");
            }
            if (orderItem.sellerId() == null) {
                throw new InvalidCardPaymentRequestException("주문 결제 검증 결과에 판매자 ID가 없습니다.");
            }
            if (orderItem.lineAmount() == null || orderItem.lineAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new InvalidCardPaymentRequestException("주문 결제 검증 결과의 주문 항목 금액이 올바르지 않습니다.");
            }
            orderItemsTotalAmount = orderItemsTotalAmount.add(orderItem.lineAmount());
        }
        return orderItemsTotalAmount;
    }

    private List<CardTransaction> createPendingTransactions(
            CardPaymentConfirmCommand command,
            List<OrderPaymentValidationItemData> orderItems,
            UUID transactionGroupId,
            java.time.LocalDateTime requestedAt
    ) {
        return orderItems.stream()
                .map(orderItem -> CardTransaction.pendingPayment(
                        identifierGenerator.generateUuid(),
                        transactionGroupId,
                        orderItem.orderItemId(),
                        command.buyerId(),
                        command.orderId().toString(),
                        orderItem.lineAmount(),
                        requestedAt
                ))
                .toList();
    }

    private List<Escrow> createHeldEscrows(
            CardPaymentConfirmCommand command,
            List<OrderPaymentValidationItemData> orderItems,
            LocalDateTime createdAt
    ) {
        return orderItems.stream()
                .map(orderItem -> Escrow.createHeld(
                        identifierGenerator.generateUuid(),
                        command.orderId(),
                        orderItem.orderItemId(),
                        EscrowReferenceType.ORDER_ITEM,
                        command.buyerId(),
                        orderItem.sellerId(),
                        orderItem.lineAmount(),
                        createdAt
                ))
                .toList();
    }

    private void validateConfirmation(
            CardPaymentConfirmCommand command,
            TossPaymentGateway.TossPaymentConfirmation confirmation
    ) {
        if (!Objects.equals(confirmation.orderId(), command.orderId().toString())) {
            throw new InvalidCardPaymentRequestException("승인된 주문 ID가 요청 주문 ID와 일치하지 않습니다.");
        }
        if (confirmation.approvedAmount() == null
                || command.amount() == null
                || confirmation.approvedAmount().compareTo(command.amount()) != 0) {
            throw new InvalidCardPaymentRequestException("승인된 결제 금액이 요청 금액과 일치하지 않습니다.");
        }
        if (!isAllowedPgMethod(confirmation.method())) {
            throw new InvalidCardPaymentRequestException("승인된 결제 수단이 카드가 아닙니다.");
        }
    }

    private boolean isAllowedPgMethod(String method) {
        return CARD_METHOD.equals(method) || EASY_PAY_METHOD.equals(method);
    }

    private void failTransactions(List<CardTransaction> cardTransactions, String failureReason) {
        var failedAt = timeProvider.now();
        String resolvedFailureReason = resolveFailureReason(failureReason);
        for (CardTransaction cardTransaction : cardTransactions) {
            cardTransaction.fail(null, resolvedFailureReason, failedAt);
        }
        cardTransactionRepository.saveAll(cardTransactions);
    }

    private String resolveFailureReason(String failureReason) {
        if (failureReason == null || failureReason.isBlank()) {
            return "카드 결제 승인 처리에 실패했습니다.";
        }
        return failureReason;
    }

    private TossPaymentGateway.TossPaymentConfirmation confirmWithToss(
            CardPaymentConfirmCommand command,
            List<CardTransaction> cardTransactions
    ) {
        try {
            return tossPaymentGateway.confirm(
                    command.paymentKey(),
                    command.orderId().toString(),
                    command.amount()
            );
        } catch (PaymentGatewayException exception) {
            failTransactions(cardTransactions, exception.getMessage());
            throw exception;
        } catch (RuntimeException exception) {
            failTransactions(cardTransactions, exception.getMessage());
            throw exception;
        }
    }

    private void publishCardConfirmSuccess(UUID orderId) {
        cardConfirmResultEventPublisher.publish(new CardConfirmResultMessage(
                identifierGenerator.generateUuid(),
                orderId,
                CardConfirmResultStatus.SUCCESS,
                null,
                Instant.now()
        ));
    }

    private void publishCardConfirmFailure(UUID orderId, String failReason) {
        cardConfirmResultEventPublisher.publish(new CardConfirmResultMessage(
                identifierGenerator.generateUuid(),
                orderId,
                CardConfirmResultStatus.FAILED,
                failReason,
                Instant.now()
        ));
    }

    private String resolveFailReason(RuntimeException exception) {
        if (exception == null) {
            return "카드 결제 승인 처리에 실패했습니다.";
        }
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return "카드 결제 승인 처리에 실패했습니다.";
        }
        return message;
    }

    private void saveOrderPaymentRecords(
            CardPaymentConfirmCommand command,
            UUID cardTransactionGroupId,
            LocalDateTime paidAt
    ) {
        OrderPayment orderPayment = OrderPayment.createSucceeded(
                identifierGenerator.generateUuid(),
                command.orderId(),
                command.buyerId(),
                command.amount(),
                OrderPaymentMethod.CARD,
                paidAt
        );
        OrderPayment savedOrderPayment = orderPaymentRepository.save(orderPayment);

        OrderPaymentAllocation cardAllocation = OrderPaymentAllocation.cardAllocation(
                identifierGenerator.generateUuid(),
                savedOrderPayment.getOrderPaymentId(),
                command.amount(),
                cardTransactionGroupId,
                paidAt
        );
        orderPaymentAllocationRepository.saveAll(java.util.List.of(cardAllocation));
    }
}
