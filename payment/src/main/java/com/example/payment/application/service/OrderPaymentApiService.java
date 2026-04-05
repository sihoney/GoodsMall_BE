package com.example.payment.application.service;

import com.example.payment.application.dto.OrderPaymentCommand;
import com.example.payment.application.dto.OrderPaymentResult;
import com.example.payment.application.dto.OrderPaymentSellerCommand;
import com.example.payment.application.usecase.OrderPaymentApiUseCase;
import com.example.payment.application.usecase.OrderPaymentUseCase;
import com.example.payment.common.exception.InvalidOrderPaymentRequestException;
import com.example.payment.common.exception.WalletNotFoundException;
import com.example.payment.domain.service.OrderPaymentResultEventPublisher;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentFailureReason;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentResultMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentResultStatus;
import com.example.payment.presentation.dto.request.OrderPaymentApiRequest;
import com.example.payment.presentation.dto.request.OrderPaymentApiOrderLineRequest;
import com.example.payment.presentation.dto.response.OrderPaymentApiResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * 주문 결제 API 진입 서비스다.
 * 기존 Kafka consumer가 수행하던 요청 검증, seller 집계, 실패 코드 매핑을 담당한다.
 */
@Service
public class OrderPaymentApiService implements OrderPaymentApiUseCase {

    private final OrderPaymentUseCase orderPaymentUseCase;
    private final OrderPaymentResultEventPublisher orderPaymentResultEventPublisher;

    public OrderPaymentApiService(
            OrderPaymentUseCase orderPaymentUseCase,
            OrderPaymentResultEventPublisher orderPaymentResultEventPublisher
    ) {
        this.orderPaymentUseCase = orderPaymentUseCase;
        this.orderPaymentResultEventPublisher = orderPaymentResultEventPublisher;
    }

    @Override
    public OrderPaymentApiResponse payOrder(OrderPaymentApiRequest request) {
        validateRequest(request);
        // todo : 요청 기록 시간이 필요한 경우 parsInt 값을 localtime으로 변경해야함
        try {
            OrderPaymentResult result = orderPaymentUseCase.payOrder(new OrderPaymentCommand(
                    request.orderId(),
                    request.buyerId(),
                    toAmount(request.totalPrice()),
                    // todo : 부분 취소, 부분 환불에 기능을 추가할 경우 변경이 필요.
                    aggregateSellerPayments(request.orderLines()),
                    null
            ));
            return successResponse(request, result);
        } catch (WalletNotFoundException e) {
            return failureResponse(request, OrderPaymentFailureReason.WALLET_NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return failureResponse(request, OrderPaymentFailureReason.INSUFFICIENT_BALANCE);
        } catch (InvalidOrderPaymentRequestException e) {
            return failureResponse(request, OrderPaymentFailureReason.INVALID_REQUEST);
        } catch (RuntimeException e) {
            return failureResponse(request, OrderPaymentFailureReason.INTERNAL_ERROR);
        }
    }

    private OrderPaymentApiResponse successResponse(OrderPaymentApiRequest request, OrderPaymentResult result) {
        orderPaymentResultEventPublisher.publish(new OrderPaymentResultMessage(
                UUID.randomUUID(),
                request.orderId(),
                request.buyerId(),
                request.totalPrice(),
                OrderPaymentResultStatus.SUCCESS,
                null,
                Instant.now()
        ));

        return new OrderPaymentApiResponse(
                request.orderId(),
                request.buyerId(),
                request.totalPrice(),
                OrderPaymentResultStatus.SUCCESS.name(),
                null
        );
    }

    private OrderPaymentApiResponse failureResponse(
            OrderPaymentApiRequest request,
            OrderPaymentFailureReason reason
    ) {
        orderPaymentResultEventPublisher.publish(new OrderPaymentResultMessage(
                UUID.randomUUID(),
                request.orderId(),
                request.buyerId(),
                request.totalPrice(),
                OrderPaymentResultStatus.FAILED,
                reason,
                Instant.now()
        ));

        return new OrderPaymentApiResponse(
                request.orderId(),
                request.buyerId(),
                request.totalPrice(),
                OrderPaymentResultStatus.FAILED.name(),
                reason.name()
        );
    }

    private void validateRequest(OrderPaymentApiRequest request) {
        if (request == null) {
            throw new InvalidOrderPaymentRequestException("orderPaymentApi request is required.");
        }
        if (request.orderId() == null) {
            throw new InvalidOrderPaymentRequestException("orderId is required.");
        }
        if (request.buyerId() == null) {
            throw new InvalidOrderPaymentRequestException("buyerId is required.");
        }
        if (request.totalPrice() == null || request.totalPrice().signum() <= 0) {
            throw new InvalidOrderPaymentRequestException("totalPrice must be positive.");
        }
        if (request.orderLines() == null || request.orderLines().isEmpty()) {
            throw new InvalidOrderPaymentRequestException("orderLines must not be empty.");
        }

        BigDecimal lineTotalAmount = BigDecimal.ZERO;
        for (OrderPaymentApiOrderLineRequest orderLine : request.orderLines()) {
            if (orderLine == null) {
                throw new InvalidOrderPaymentRequestException("orderLines must not contain null.");
            }
            if (orderLine.orderItemId() == null) {
                throw new InvalidOrderPaymentRequestException("orderItemId is required.");
            }
            if (orderLine.sellerId() == null) {
                throw new InvalidOrderPaymentRequestException("sellerId is required.");
            }
            if (orderLine.quantity() == null || orderLine.quantity() <= 0) {
                throw new InvalidOrderPaymentRequestException("quantity must be positive.");
            }
            if (orderLine.lineTotalPrice() == null || orderLine.lineTotalPrice().signum() <= 0) {
                throw new InvalidOrderPaymentRequestException("lineTotalPrice must be positive.");
            }
            lineTotalAmount = lineTotalAmount.add(orderLine.lineTotalPrice());
        }

        if (lineTotalAmount.compareTo(request.totalPrice()) != 0) {
            throw new InvalidOrderPaymentRequestException("lineTotalPrice total must equal totalPrice.");
        }
    }

    // orderLines를 seller 기준으로 집계해 sellerId - sellerReceivableAmount 매핑으로 변환한다.
    private List<OrderPaymentSellerCommand> aggregateSellerPayments(List<OrderPaymentApiOrderLineRequest> orderLines) {
        Map<UUID, Long> sellerPaymentMap = orderLines.stream()
                .collect(Collectors.groupingBy(
                        OrderPaymentApiOrderLineRequest::sellerId,
                        Collectors.summingLong(line -> toAmount(line.lineTotalPrice()))
                ));

        return sellerPaymentMap.entrySet().stream()
                .map(entry -> new OrderPaymentSellerCommand(entry.getKey(), entry.getValue()))
                .toList();
    }

    // long 값을 BigDecimal로 변경
    private Long toAmount(BigDecimal amount) {
        try {
            return amount.longValueExact();
        } catch (ArithmeticException e) {
            throw new InvalidOrderPaymentRequestException("price must be an exact whole amount.");
        }
    }
}
