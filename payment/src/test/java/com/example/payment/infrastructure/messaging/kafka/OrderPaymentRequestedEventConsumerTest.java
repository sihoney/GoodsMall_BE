package com.example.payment.infrastructure.messaging.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.example.payment.application.dto.OrderPaymentCommand;
import com.example.payment.application.dto.OrderPaymentResult;
import com.example.payment.application.dto.OrderPaymentSellerCommand;
import com.example.payment.application.usecase.OrderPaymentUseCase;
import com.example.payment.common.exception.InvalidOrderPaymentRequestException;
import com.example.payment.common.exception.WalletNotFoundException;
import com.example.payment.domain.service.OrderPaymentResultEventPublisher;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentFailureReason;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentRequestedLineMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentRequestedMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentResultMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentResultStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderPaymentRequestedEventConsumer 테스트")
class OrderPaymentRequestedEventConsumerTest {

    @Mock
    private OrderPaymentUseCase orderPaymentUseCase;

    @Mock
    private OrderPaymentResultEventPublisher orderPaymentResultEventPublisher;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OrderPaymentRequestedEventConsumer consumer;

    @Test
    @DisplayName("정상 이벤트를 수신하면 주문 결제 usecase를 호출한다")
    void listen_validEvent_callsOrderPaymentUseCase() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID buyerMemberId = UUID.randomUUID();
        UUID sellerMemberId = UUID.randomUUID();
        OrderPaymentRequestedMessage event = new OrderPaymentRequestedMessage(
                "evt-1",
                orderId,
                buyerMemberId,
                BigDecimal.valueOf(12_000L),
                Instant.parse("2026-03-31T12:45:30Z"),
                Instant.parse("2026-03-31T12:47:30Z"),
                List.of(new OrderPaymentRequestedLineMessage(
                        UUID.randomUUID(),
                        sellerMemberId,
                        BigDecimal.valueOf(12_000L),
                        1,
                        BigDecimal.valueOf(12_000L)
                ))
        );
        OrderPaymentResult result = new OrderPaymentResult(
                orderId,
                UUID.randomUUID(),
                List.of(UUID.randomUUID()),
                12_000L,
                8_000L
        );
        String eventJson = "{\"eventId\":\"evt-1\"}";

        given(objectMapper.readValue(eventJson, OrderPaymentRequestedMessage.class)).willReturn(event);
        given(orderPaymentUseCase.payOrder(any(OrderPaymentCommand.class))).willReturn(result);

        consumer.listen(eventJson);

        ArgumentCaptor<OrderPaymentCommand> captor = ArgumentCaptor.forClass(OrderPaymentCommand.class);
        verify(orderPaymentUseCase).payOrder(captor.capture());
        assertThat(captor.getValue().orderId()).isEqualTo(orderId);
        assertThat(captor.getValue().buyerMemberId()).isEqualTo(buyerMemberId);
        assertThat(captor.getValue().orderAmount()).isEqualTo(12_000L);
        assertThat(captor.getValue().sellerPayments())
                .containsExactly(new OrderPaymentSellerCommand(sellerMemberId, 12_000L));

        ArgumentCaptor<OrderPaymentResultMessage> resultCaptor = ArgumentCaptor.forClass(OrderPaymentResultMessage.class);
        verify(orderPaymentResultEventPublisher).publish(resultCaptor.capture());
        assertThat(resultCaptor.getValue().status()).isEqualTo(OrderPaymentResultStatus.SUCCESS);
        assertThat(resultCaptor.getValue().buyerMemberId()).isEqualTo(buyerMemberId);
        assertThat(resultCaptor.getValue().amount()).isEqualByComparingTo(BigDecimal.valueOf(12_000L));
        assertThat(resultCaptor.getValue().reasonCode()).isNull();
    }

    @Test
    @DisplayName("seller가 여러 명인 주문도 주문 단위 결과 이벤트로 발행한다")
    void listen_multiSellerEvent_aggregatesSellerPayments() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID buyerMemberId = UUID.randomUUID();
        UUID firstSellerId = UUID.randomUUID();
        UUID secondSellerId = UUID.randomUUID();
        OrderPaymentRequestedMessage event = new OrderPaymentRequestedMessage(
                "evt-1",
                orderId,
                buyerMemberId,
                BigDecimal.valueOf(15_000L),
                Instant.parse("2026-03-31T12:45:30Z"),
                Instant.parse("2026-03-31T12:47:30Z"),
                List.of(
                        new OrderPaymentRequestedLineMessage(
                                UUID.randomUUID(),
                                firstSellerId,
                                BigDecimal.valueOf(5_000L),
                                2,
                                BigDecimal.valueOf(10_000L)
                        ),
                        new OrderPaymentRequestedLineMessage(
                                UUID.randomUUID(),
                                secondSellerId,
                                BigDecimal.valueOf(5_000L),
                                1,
                                BigDecimal.valueOf(5_000L)
                        )
                )
        );
        OrderPaymentResult result = new OrderPaymentResult(
                orderId,
                UUID.randomUUID(),
                List.of(UUID.randomUUID(), UUID.randomUUID()),
                15_000L,
                5_000L
        );
        String eventJson = "{\"eventId\":\"evt-1\"}";

        given(objectMapper.readValue(eventJson, OrderPaymentRequestedMessage.class)).willReturn(event);
        given(orderPaymentUseCase.payOrder(any(OrderPaymentCommand.class))).willReturn(result);

        consumer.listen(eventJson);

        ArgumentCaptor<OrderPaymentCommand> captor = ArgumentCaptor.forClass(OrderPaymentCommand.class);
        verify(orderPaymentUseCase).payOrder(captor.capture());
        assertThat(captor.getValue().sellerPayments())
                .containsExactlyInAnyOrder(
                        new OrderPaymentSellerCommand(firstSellerId, 10_000L),
                        new OrderPaymentSellerCommand(secondSellerId, 5_000L)
                );

        ArgumentCaptor<OrderPaymentResultMessage> resultCaptor = ArgumentCaptor.forClass(OrderPaymentResultMessage.class);
        verify(orderPaymentResultEventPublisher).publish(resultCaptor.capture());
        assertThat(resultCaptor.getValue().status()).isEqualTo(OrderPaymentResultStatus.SUCCESS);
        assertThat(resultCaptor.getValue().buyerMemberId()).isEqualTo(buyerMemberId);
        assertThat(resultCaptor.getValue().amount()).isEqualByComparingTo(BigDecimal.valueOf(15_000L));
    }

    @Test
    @DisplayName("wallet이 없으면 FAILED 결과 이벤트를 발행한다")
    void listen_walletNotFound_publishesFailedResult() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID buyerMemberId = UUID.randomUUID();
        UUID sellerMemberId = UUID.randomUUID();
        OrderPaymentRequestedMessage event = new OrderPaymentRequestedMessage(
                "evt-1",
                orderId,
                buyerMemberId,
                BigDecimal.valueOf(12_000L),
                Instant.parse("2026-03-31T12:45:30Z"),
                Instant.parse("2026-03-31T12:47:30Z"),
                List.of(new OrderPaymentRequestedLineMessage(
                        UUID.randomUUID(),
                        sellerMemberId,
                        BigDecimal.valueOf(12_000L),
                        1,
                        BigDecimal.valueOf(12_000L)
                ))
        );
        String eventJson = "{\"eventId\":\"evt-1\"}";
        given(objectMapper.readValue(eventJson, OrderPaymentRequestedMessage.class)).willReturn(event);

        doThrow(new WalletNotFoundException()).when(orderPaymentUseCase)
                .payOrder(any(OrderPaymentCommand.class));

        consumer.listen(eventJson);

        ArgumentCaptor<OrderPaymentResultMessage> captor = ArgumentCaptor.forClass(OrderPaymentResultMessage.class);
        verify(orderPaymentResultEventPublisher).publish(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(OrderPaymentResultStatus.FAILED);
        assertThat(captor.getValue().reasonCode()).isEqualTo(OrderPaymentFailureReason.WALLET_NOT_FOUND);
        assertThat(captor.getValue().amount()).isEqualByComparingTo(BigDecimal.valueOf(12_000L));
    }

    @Test
    @DisplayName("buyerId가 없으면 예외가 발생한다")
    void listen_missingBuyerId_throwsException() throws Exception {
        OrderPaymentRequestedMessage event = new OrderPaymentRequestedMessage(
                "evt-1",
                UUID.randomUUID(),
                null,
                BigDecimal.valueOf(12_000L),
                Instant.parse("2026-03-31T12:45:30Z"),
                Instant.parse("2026-03-31T12:47:30Z"),
                List.of(new OrderPaymentRequestedLineMessage(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        BigDecimal.valueOf(12_000L),
                        1,
                        BigDecimal.valueOf(12_000L)
                ))
        );
        String eventJson = "{\"eventId\":\"evt-1\"}";
        given(objectMapper.readValue(eventJson, OrderPaymentRequestedMessage.class)).willReturn(event);

        assertThatThrownBy(() -> consumer.listen(eventJson))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(InvalidOrderPaymentRequestException.class)
                .hasMessageContaining("Failed to deserialize OrderPaymentRequestedMessage");

        verifyNoInteractions(orderPaymentUseCase);
    }
}
