package com.example.payment.application.listener;

import com.example.payment.application.dto.EscrowReleaseCommand;
import com.example.payment.application.event.OrderPurchaseConfirmedEvent;
import com.example.payment.application.usecase.EscrowReleaseUseCase;
import com.example.payment.domain.enumtype.ConfirmationType;
import com.example.payment.domain.exception.InvalidOrderPaymentRequestException;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class OrderPurchaseConfirmedEventListener {

    private final EscrowReleaseUseCase escrowReleaseUseCase;

    public OrderPurchaseConfirmedEventListener(EscrowReleaseUseCase escrowReleaseUseCase) {
        this.escrowReleaseUseCase = escrowReleaseUseCase;
    }

    @EventListener
    public void handle(OrderPurchaseConfirmedEvent event) {
        validateEvent(event);

        escrowReleaseUseCase.releaseEscrow(
                new EscrowReleaseCommand(
                        event.orderId(),
                        event.sellerMemberId()
                )
        );
    }

    private void validateEvent(OrderPurchaseConfirmedEvent event) {
        if (event.orderId() == null) {
            throw new InvalidOrderPaymentRequestException("orderId is required.");
        }
        if (event.sellerMemberId() == null) {
            throw new InvalidOrderPaymentRequestException("sellerMemberId is required.");
        }
        if (event.confirmationType() != ConfirmationType.MANUAL) {
            throw new InvalidOrderPaymentRequestException("Only MANUAL confirmation event is allowed.");
        }
    }
}
