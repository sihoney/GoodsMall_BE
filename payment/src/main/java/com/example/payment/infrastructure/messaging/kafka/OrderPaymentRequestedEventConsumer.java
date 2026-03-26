package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.dto.OrderPaymentResult;
import com.example.payment.application.dto.OrderPaymentCommand;
import com.example.payment.application.usecase.OrderPaymentUseCase;
import com.example.payment.domain.entity.Escrow;
import com.example.payment.domain.entity.Wallet;
import com.example.payment.domain.exception.InvalidOrderPaymentRequestException;
import com.example.payment.domain.exception.OrderPaymentAlreadyCompletedException;
import com.example.payment.domain.exception.WalletNotFoundException;
import com.example.payment.domain.repository.EscrowRepository;
import com.example.payment.domain.repository.WalletRepository;
import com.example.payment.domain.service.OrderPaymentResultEventPublisher;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentFailureReason;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentRequestedMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentResultMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentResultStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderPaymentRequestedEventConsumer {

    private final OrderPaymentUseCase orderPaymentUseCase;
    private final OrderPaymentResultEventPublisher orderPaymentResultEventPublisher;
    private final EscrowRepository escrowRepository;
    private final WalletRepository walletRepository;

    public OrderPaymentRequestedEventConsumer(
            OrderPaymentUseCase orderPaymentUseCase,
            OrderPaymentResultEventPublisher orderPaymentResultEventPublisher,
            EscrowRepository escrowRepository,
            WalletRepository walletRepository
    ) {
        this.orderPaymentUseCase = orderPaymentUseCase;
        this.orderPaymentResultEventPublisher = orderPaymentResultEventPublisher;
        this.escrowRepository = escrowRepository;
        this.walletRepository = walletRepository;
    }

    @KafkaListener(
            topics = "${payment.kafka.topics.order-payment-requested:order.payment-requested}",
            groupId = "${payment.kafka.consumer-groups.order-payment-requested:payment-service}",
            containerFactory = "orderPaymentRequestedKafkaListenerContainerFactory"
    )
    public void listen(OrderPaymentRequestedMessage event) {
        validateEvent(event);

        try {
            OrderPaymentResult result = orderPaymentUseCase.payOrder(new OrderPaymentCommand(
                    event.orderId(),
                    event.buyerMemberId(),
                    event.sellerMemberId(),
                    event.orderAmount(),
                    event.sellerReceivableAmount(),
                    null
            ));
            orderPaymentResultEventPublisher.publish(successMessage(event, result));
        } catch (OrderPaymentAlreadyCompletedException ignored) {
            orderPaymentResultEventPublisher.publish(duplicateSuccessMessage(event));
        } catch (WalletNotFoundException e) {
            orderPaymentResultEventPublisher.publish(failureMessage(
                    event,
                    OrderPaymentFailureReason.WALLET_NOT_FOUND,
                    e.getMessage()
            ));
        } catch (IllegalArgumentException e) {
            orderPaymentResultEventPublisher.publish(failureMessage(
                    event,
                    OrderPaymentFailureReason.INSUFFICIENT_BALANCE,
                    e.getMessage()
            ));
        } catch (InvalidOrderPaymentRequestException e) {
            orderPaymentResultEventPublisher.publish(failureMessage(
                    event,
                    OrderPaymentFailureReason.INVALID_REQUEST,
                    e.getMessage()
            ));
        } catch (RuntimeException e) {
            orderPaymentResultEventPublisher.publish(failureMessage(
                    event,
                    OrderPaymentFailureReason.INTERNAL_ERROR,
                    e.getMessage()
            ));
        }
    }

    private OrderPaymentResultMessage successMessage(OrderPaymentRequestedMessage event, OrderPaymentResult result) {
        return new OrderPaymentResultMessage(
                newEventId(),
                event.orderId(),
                event.buyerMemberId(),
                event.sellerMemberId(),
                OrderPaymentResultStatus.SUCCESS,
                event.orderAmount(),
                event.sellerReceivableAmount(),
                result.buyerWalletId(),
                result.escrowId(),
                null,
                null,
                LocalDateTime.now()
        );
    }

    private OrderPaymentResultMessage failureMessage(
            OrderPaymentRequestedMessage event,
            OrderPaymentFailureReason reason,
            String failureMessage
    ) {
        return new OrderPaymentResultMessage(
                newEventId(),
                event.orderId(),
                event.buyerMemberId(),
                event.sellerMemberId(),
                OrderPaymentResultStatus.FAILED,
                event.orderAmount(),
                event.sellerReceivableAmount(),
                null,
                null,
                reason,
                failureMessage,
                LocalDateTime.now()
        );
    }

    private OrderPaymentResultMessage duplicateSuccessMessage(OrderPaymentRequestedMessage event) {
        UUID escrowId = escrowRepository.findByOrderId(event.orderId())
                .map(Escrow::getEscrowId)
                .orElse(null);
        UUID buyerWalletId = walletRepository.findByMemberId(event.buyerMemberId())
                .map(Wallet::getWalletId)
                .orElse(null);

        return new OrderPaymentResultMessage(
                newEventId(),
                event.orderId(),
                event.buyerMemberId(),
                event.sellerMemberId(),
                OrderPaymentResultStatus.SUCCESS,
                event.orderAmount(),
                event.sellerReceivableAmount(),
                buyerWalletId,
                escrowId,
                null,
                null,
                LocalDateTime.now()
        );
    }

    private String newEventId() {
        return UUID.randomUUID().toString();
    }

    private void validateEvent(OrderPaymentRequestedMessage event) {
        if (event == null) {
            throw new InvalidOrderPaymentRequestException("orderPaymentRequested event is required.");
        }
        if (event.orderId() == null) {
            throw new InvalidOrderPaymentRequestException("orderId is required.");
        }
        if (event.buyerMemberId() == null) {
            throw new InvalidOrderPaymentRequestException("buyerMemberId is required.");
        }
        if (event.sellerMemberId() == null) {
            throw new InvalidOrderPaymentRequestException("sellerMemberId is required.");
        }
        if (event.orderAmount() == null || event.orderAmount() <= 0) {
            throw new InvalidOrderPaymentRequestException("orderAmount must be positive.");
        }
        if (event.sellerReceivableAmount() == null || event.sellerReceivableAmount() <= 0) {
            throw new InvalidOrderPaymentRequestException("sellerReceivableAmount must be positive.");
        }
    }
}
