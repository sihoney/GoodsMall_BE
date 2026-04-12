package com.example.payment.application.service;

import com.example.payment.application.dto.CardPaymentConfirmCommand;
import com.example.payment.application.dto.CardPaymentConfirmOrderItemCommand;
import com.example.payment.application.dto.CardPaymentConfirmResult;
import com.example.payment.application.usecase.CardPaymentConfirmUseCase;
import com.example.payment.common.exception.InvalidCardPaymentRequestException;
import com.example.payment.common.exception.PaymentGatewayException;
import com.example.payment.domain.entity.CardTransaction;
import com.example.payment.domain.enumtype.CardTransactionStatus;
import com.example.payment.domain.repository.CardTransactionRepository;
import com.example.payment.domain.service.IdentifierGenerator;
import com.example.payment.domain.service.TimeProvider;
import com.example.payment.domain.service.TossPaymentGateway;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CardPaymentConfirmService implements CardPaymentConfirmUseCase {

    private static final String CARD_METHOD = "카드";

    private final CardTransactionRepository cardTransactionRepository;
    private final TossPaymentGateway tossPaymentGateway;
    private final IdentifierGenerator identifierGenerator;
    private final TimeProvider timeProvider;

    public CardPaymentConfirmService(
            CardTransactionRepository cardTransactionRepository,
            TossPaymentGateway tossPaymentGateway,
            IdentifierGenerator identifierGenerator,
            TimeProvider timeProvider
    ) {
        this.cardTransactionRepository = cardTransactionRepository;
        this.tossPaymentGateway = tossPaymentGateway;
        this.identifierGenerator = identifierGenerator;
        this.timeProvider = timeProvider;
    }

    @Override
    public CardPaymentConfirmResult confirmCardPayment(CardPaymentConfirmCommand command) {
        validateCommand(command);

        UUID transactionGroupId = identifierGenerator.generateUuid();
        var requestedAt = timeProvider.now();
        List<CardTransaction> cardTransactions = command.orderItems().stream()
                .map(orderItem -> CardTransaction.pendingPayment(
                        identifierGenerator.generateUuid(),
                        transactionGroupId,
                        orderItem.orderItemId(),
                        command.buyerId(),
                        command.orderId().toString(),
                        orderItem.amount(),
                        requestedAt
                ))
                .toList();

        cardTransactionRepository.saveAll(cardTransactions);

        TossPaymentGateway.TossPaymentConfirmation confirmation;
        try {
            confirmation = tossPaymentGateway.confirm(
                    command.paymentKey(),
                    command.orderId().toString(),
                    command.amount()
            );
        } catch (PaymentGatewayException e) {
            failTransactions(cardTransactions, e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            failTransactions(cardTransactions, e.getMessage());
            throw e;
        }

        try {
            validateConfirmation(command, confirmation);
        } catch (RuntimeException e) {
            failTransactions(cardTransactions, e.getMessage());
            throw e;
        }

        for (CardTransaction cardTransaction : cardTransactions) {
            cardTransaction.approve(
                    confirmation.paymentKey(),
                    cardTransaction.getRequestedAmount(),
                    0L,
                    confirmation.approvedAt()
            );
        }

        cardTransactionRepository.saveAll(cardTransactions);

        return new CardPaymentConfirmResult(
                transactionGroupId,
                command.orderId(),
                command.buyerId(),
                confirmation.approvedAmount(),
                CardTransactionStatus.SUCCESS,
                confirmation.approvedAt()
        );
    }

    private void validateCommand(CardPaymentConfirmCommand command) {
        if (command == null) {
            throw new InvalidCardPaymentRequestException("card payment confirm request is required.");
        }
        if (command.buyerId() == null) {
            throw new InvalidCardPaymentRequestException("buyerId is required.");
        }
        if (command.orderId() == null) {
            throw new InvalidCardPaymentRequestException("orderId is required.");
        }
        if (command.paymentKey() == null || command.paymentKey().isBlank()) {
            throw new InvalidCardPaymentRequestException("paymentKey is required.");
        }
        if (command.amount() == null || command.amount() <= 0) {
            throw new InvalidCardPaymentRequestException("amount must be positive.");
        }
        if (command.orderItems() == null || command.orderItems().isEmpty()) {
            throw new InvalidCardPaymentRequestException("orderItems must not be empty.");
        }

        long itemTotalAmount = 0L;
        for (CardPaymentConfirmOrderItemCommand orderItem : command.orderItems()) {
            if (orderItem == null) {
                throw new InvalidCardPaymentRequestException("orderItems must not contain null.");
            }
            if (orderItem.orderItemId() == null) {
                throw new InvalidCardPaymentRequestException("orderItemId is required.");
            }
            if (orderItem.amount() == null || orderItem.amount() <= 0) {
                throw new InvalidCardPaymentRequestException("order item amount must be positive.");
            }
            itemTotalAmount += orderItem.amount();
        }

        if (!Objects.equals(command.amount(), itemTotalAmount)) {
            throw new InvalidCardPaymentRequestException("sum of order item amounts must equal amount.");
        }
    }

    private void validateConfirmation(
            CardPaymentConfirmCommand command,
            TossPaymentGateway.TossPaymentConfirmation confirmation
    ) {
        if (!Objects.equals(confirmation.orderId(), command.orderId().toString())) {
            throw new InvalidCardPaymentRequestException("confirmed orderId does not match request.");
        }
        if (!Objects.equals(confirmation.approvedAmount(), command.amount())) {
            throw new InvalidCardPaymentRequestException("confirmed amount does not match request.");
        }
        if (!CARD_METHOD.equals(confirmation.method())) {
            throw new InvalidCardPaymentRequestException("confirmed payment method is not card.");
        }
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
            return "Card payment confirmation failed.";
        }
        return failureReason;
    }
}
