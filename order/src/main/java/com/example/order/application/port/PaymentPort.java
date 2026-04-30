package com.example.order.application.port;

import com.example.order.application.port.dto.request.PaymentRefundRequest;
import com.example.order.application.port.dto.request.PaymentRequest;
import com.example.order.application.port.dto.request.SellerRefundRequest;
import com.example.order.application.port.dto.response.PaymentRefundResult;
import com.example.order.application.port.dto.response.PaymentResult;

public interface PaymentPort {

    PaymentResult requestPayment(PaymentRequest request);

    PaymentRefundResult requestRefund(PaymentRefundRequest request);

    PaymentRefundResult requestSellerRefund(SellerRefundRequest request);

}
