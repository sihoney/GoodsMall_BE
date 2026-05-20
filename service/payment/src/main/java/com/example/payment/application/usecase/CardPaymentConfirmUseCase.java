package com.example.payment.application.usecase;

import com.example.payment.application.dto.CardPaymentConfirmCommand;
import com.example.payment.application.dto.CardPaymentConfirmResult;

public interface CardPaymentConfirmUseCase {

    CardPaymentConfirmResult confirmCardPayment(CardPaymentConfirmCommand command);
}
