package com.example.payment.card.application.usecase;

import com.example.payment.card.application.dto.CardPaymentConfirmCommand;
import com.example.payment.card.application.dto.CardPaymentConfirmResult;

public interface CardPaymentConfirmUseCase {

    CardPaymentConfirmResult confirmCardPayment(CardPaymentConfirmCommand command);
}
