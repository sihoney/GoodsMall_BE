package com.example.payment.orderpayment.application.service;

import com.example.payment.orderpayment.application.dto.OrderPaymentCommand;
import com.example.payment.orderpayment.application.dto.OrderPaymentLineCommand;
import com.example.payment.orderpayment.application.dto.OrderPaymentResult;
import com.example.payment.orderpayment.application.usecase.OrderPaymentApiUseCase;
import com.example.payment.orderpayment.application.usecase.OrderPaymentUseCase;
import com.example.payment.common.common.exception.InvalidOrderPaymentRequestException;
import com.example.payment.common.common.exception.WalletNotFoundException;
import com.example.payment.common.infrastructure.messaging.kafka.contract.OrderPaymentFailureReason;
import com.example.payment.common.infrastructure.messaging.kafka.contract.OrderPaymentResultMessage;
import com.example.payment.common.infrastructure.messaging.kafka.contract.OrderPaymentResultStatus;
import com.example.payment.outbox.infrastructure.messaging.kafka.OrderPaymentResultOutboxEventSaver;
import com.example.payment.orderpayment.presentation.dto.request.OrderPaymentApiRequest;
import com.example.payment.orderpayment.presentation.dto.request.OrderPaymentApiOrderLineRequest;
import com.example.payment.orderpayment.presentation.dto.response.OrderPaymentApiResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * 二쇰Ц 寃곗젣 API 吏꾩엯 ?쒕퉬?ㅻ떎.
 * 湲곗〈 Kafka consumer媛 ?섑뻾?섎뜕 ?붿껌 寃利? orderItem ?⑥쐞 ?뺢퇋?? ?ㅽ뙣 肄붾뱶 留ㅽ븨???대떦?쒕떎.
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
            throw new InvalidOrderPaymentRequestException("二쇰Ц 寃곗젣 API ?붿껌? ?꾩닔?낅땲??");
        }
        if (request.orderId() == null) {
            throw new InvalidOrderPaymentRequestException("二쇰Ц ID???꾩닔?낅땲??");
        }
        if (request.buyerId() == null) {
            throw new InvalidOrderPaymentRequestException("援щℓ??ID???꾩닔?낅땲??");
        }
        if (request.totalPrice() == null || request.totalPrice().signum() <= 0) {
            throw new InvalidOrderPaymentRequestException("珥?寃곗젣 湲덉븸? 0蹂대떎 而ㅼ빞 ?⑸땲??");
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
            throw new InvalidOrderPaymentRequestException("二쇰Ц ??ぉ ID???꾩닔?낅땲??");
            }
            if (!seenOrderItemIds.add(orderLine.orderItemId())) {
            throw new InvalidOrderPaymentRequestException("二쇰Ц ?쇱씤??二쇰Ц ??ぉ ID??以묐났?????놁뒿?덈떎.");
            }
            if (orderLine.sellerId() == null) {
            throw new InvalidOrderPaymentRequestException("?먮ℓ??ID???꾩닔?낅땲??");
            }
            if (orderLine.quantity() == null || orderLine.quantity() <= 0) {
            throw new InvalidOrderPaymentRequestException("?섎웾? 0蹂대떎 而ㅼ빞 ?⑸땲??");
            }
            if (orderLine.lineTotalPrice() == null || orderLine.lineTotalPrice().signum() <= 0) {
            throw new InvalidOrderPaymentRequestException("二쇰Ц ??ぉ 珥앹븸? 0蹂대떎 而ㅼ빞 ?⑸땲??");
            }
            lineTotalAmount = lineTotalAmount.add(orderLine.lineTotalPrice());
        }

        if (lineTotalAmount.compareTo(request.totalPrice()) != 0) {
            throw new InvalidOrderPaymentRequestException("lineTotalPrice total must equal totalPrice.");
        }
    }

    // orderLines瑜?orderItem ?⑥쐞 寃곗젣 ?낅젰?쇰줈 ?뺢퇋?뷀븳??
    private List<OrderPaymentLineCommand> toPaymentLines(List<OrderPaymentApiOrderLineRequest> orderLines) {
        return orderLines.stream()
                .map(line -> new OrderPaymentLineCommand(
                        line.orderItemId(),
                        line.sellerId(),
                        line.lineTotalPrice()
                ))
                .toList();
    }

    // BigDecimal??寃곗젣 湲덉븸?쇰줈 洹몃?濡?諛섑솚?쒕떎.
    private BigDecimal toAmount(BigDecimal amount) {
        return amount;
    }
}
