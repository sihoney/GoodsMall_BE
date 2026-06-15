package com.example.payment.payment.application.usecase;

import com.example.payment.payment.application.dto.CardPaymentConfirmCommand;
import com.example.payment.payment.application.dto.CardPaymentConfirmResult;

public interface CardPaymentConfirmUseCase {

    CardPaymentConfirmResult confirmCardPayment(CardPaymentConfirmCommand command);
}
