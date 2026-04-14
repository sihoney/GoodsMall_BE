package com.example.order.application.port;

public interface PaymentPort {

    PaymentResult requestPayment(PaymentRequest request);

}
