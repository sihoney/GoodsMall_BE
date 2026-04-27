package com.example.payment.application.service;

import com.example.payment.application.dto.OrderPaymentCommand;
import com.example.payment.application.dto.OrderPaymentLineCommand;
import com.example.payment.application.dto.OrderPaymentResult;
import com.example.payment.application.usecase.OrderPaymentApiUseCase;
import com.example.payment.application.usecase.OrderPaymentUseCase;
import com.example.payment.common.exception.InvalidOrderPaymentRequestException;
import com.example.payment.common.exception.WalletNotFoundException;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentFailureReason;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentResultMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentResultStatus;
import com.example.payment.infrastructure.messaging.kafka.OrderPaymentResultOutboxEventSaver;
import com.example.payment.presentation.dto.request.OrderPaymentApiRequest;
import com.example.payment.presentation.dto.request.OrderPaymentApiOrderLineRequest;
import com.example.payment.presentation.dto.response.OrderPaymentApiResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * 주문 결제 API 진입 서비스다.
 * 기존 Kafka consumer가 수행하던 요청 검증, orderItem 단위 정규화, 실패 코드 매핑을 담당한다.
 */
@Service
public class OrderPaymentApiService implements OrderPaymentApiUseCase {

    private final OrderPaymentUseCase orderPaymentUseCase;
    private final OrderPaymentResultOutboxEventSaver orderPaymentResultOutboxEventSaver;

    public OrderPaymentApiService(
            OrderPaymentUseCase orderPaymentUseCase,
            OrderPaymentResultOutboxEventSaver orderPaymentResultOutboxEventSaver
    ) {
        this.orderPaymentUseCase = orderPaymentUseCase;
        this.orderPaymentResultOutboxEventSaver = orderPaymentResultOutboxEventSaver;
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
                    toPaymentLines(request.orderLines())
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
        orderPaymentResultOutboxEventSaver.save(new OrderPaymentResultMessage(
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
        orderPaymentResultOutboxEventSaver.save(new OrderPaymentResultMessage(
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
            throw new InvalidOrderPaymentRequestException("주문 결제 API 요청은 필수입니다.");
        }
        if (request.orderId() == null) {
            throw new InvalidOrderPaymentRequestException("주문 ID는 필수입니다.");
        }
        if (request.buyerId() == null) {
            throw new InvalidOrderPaymentRequestException("구매자 ID는 필수입니다.");
        }
        if (request.totalPrice() == null || request.totalPrice().signum() <= 0) {
            throw new InvalidOrderPaymentRequestException("총 결제 금액은 0보다 커야 합니다.");
        }
        if (request.orderLines() == null || request.orderLines().isEmpty()) {
            throw new InvalidOrderPaymentRequestException("orderLines must not be empty.");
        }

        BigDecimal lineTotalAmount = BigDecimal.ZERO;
        Set<UUID> seenOrderItemIds = new HashSet<>();
        for (OrderPaymentApiOrderLineRequest orderLine : request.orderLines()) {
            if (orderLine == null) {
                throw new InvalidOrderPaymentRequestException("orderLines must not contain null.");
            }
            if (orderLine.orderItemId() == null) {
            throw new InvalidOrderPaymentRequestException("주문 항목 ID는 필수입니다.");
            }
            if (!seenOrderItemIds.add(orderLine.orderItemId())) {
            throw new InvalidOrderPaymentRequestException("주문 라인의 주문 항목 ID는 중복될 수 없습니다.");
            }
            if (orderLine.sellerId() == null) {
            throw new InvalidOrderPaymentRequestException("판매자 ID는 필수입니다.");
            }
            if (orderLine.quantity() == null || orderLine.quantity() <= 0) {
            throw new InvalidOrderPaymentRequestException("수량은 0보다 커야 합니다.");
            }
            if (orderLine.lineTotalPrice() == null || orderLine.lineTotalPrice().signum() <= 0) {
            throw new InvalidOrderPaymentRequestException("주문 항목 총액은 0보다 커야 합니다.");
            }
            lineTotalAmount = lineTotalAmount.add(orderLine.lineTotalPrice());
        }

        if (lineTotalAmount.compareTo(request.totalPrice()) != 0) {
            throw new InvalidOrderPaymentRequestException("lineTotalPrice total must equal totalPrice.");
        }
    }

    // orderLines를 orderItem 단위 결제 입력으로 정규화한다.
    private List<OrderPaymentLineCommand> toPaymentLines(List<OrderPaymentApiOrderLineRequest> orderLines) {
        return orderLines.stream()
                .map(line -> new OrderPaymentLineCommand(
                        line.orderItemId(),
                        line.sellerId(),
                        line.lineTotalPrice()
                ))
                .toList();
    }

    // BigDecimal을 결제 금액으로 그대로 반환한다.
    private BigDecimal toAmount(BigDecimal amount) {
        return amount;
    }
}
