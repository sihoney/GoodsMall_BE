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
import com.example.payment.application.usecase.OrderPaymentUseCase;
import com.example.payment.common.exception.InvalidOrderPaymentRequestException;
import com.example.payment.common.exception.WalletNotFoundException;
import com.example.payment.domain.service.OrderPaymentResultEventPublisher;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentFailureReason;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentRequestedMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentResultMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentResultStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
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
                sellerMemberId,
                12_000L,
                10_000L,
                LocalDateTime.of(2024, 1, 3, 10, 0, 0)
        );
        OrderPaymentResult result = new OrderPaymentResult(
                orderId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                12_000L,
                8_000L,
                null,
                null
        );
        String eventJson = "{\"eventId\":\"evt-1\"}";

        given(objectMapper.readValue(eventJson, OrderPaymentRequestedMessage.class)).willReturn(event);
        given(orderPaymentUseCase.payOrder(any(OrderPaymentCommand.class))).willReturn(result);

        consumer.listen(eventJson);

        ArgumentCaptor<OrderPaymentCommand> captor = ArgumentCaptor.forClass(OrderPaymentCommand.class);
        verify(orderPaymentUseCase).payOrder(captor.capture());
        assertThat(captor.getValue().orderId()).isEqualTo(orderId);
        assertThat(captor.getValue().buyerMemberId()).isEqualTo(buyerMemberId);
        assertThat(captor.getValue().sellerMemberId()).isEqualTo(sellerMemberId);
        assertThat(captor.getValue().orderAmount()).isEqualTo(12_000L);
        assertThat(captor.getValue().sellerReceivableAmount()).isEqualTo(10_000L);
        assertThat(captor.getValue().releaseAt()).isNull();

        ArgumentCaptor<OrderPaymentResultMessage> resultCaptor = ArgumentCaptor.forClass(OrderPaymentResultMessage.class);
        verify(orderPaymentResultEventPublisher).publish(resultCaptor.capture());
        assertThat(resultCaptor.getValue().status()).isEqualTo(OrderPaymentResultStatus.SUCCESS);
    }

    @Test
    @DisplayName("중복 결제 요청도 성공 결과를 재발행한다")
    void listen_duplicateEvent_publishesSuccessResult() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID buyerMemberId = UUID.randomUUID();
        UUID sellerMemberId = UUID.randomUUID();
        UUID escrowId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        OrderPaymentRequestedMessage event = new OrderPaymentRequestedMessage(
                "evt-1",
                orderId,
                buyerMemberId,
                sellerMemberId,
                12_000L,
                10_000L,
                LocalDateTime.of(2024, 1, 3, 10, 0, 0)
        );
        OrderPaymentResult result = new OrderPaymentResult(
                orderId,
                walletId,
                escrowId,
                12_000L,
                20_000L,
                null,
                null
        );
        String eventJson = "{\"eventId\":\"evt-1\"}";
        given(objectMapper.readValue(eventJson, OrderPaymentRequestedMessage.class)).willReturn(event);
        given(orderPaymentUseCase.payOrder(any(OrderPaymentCommand.class))).willReturn(result);

        consumer.listen(eventJson);

        ArgumentCaptor<OrderPaymentResultMessage> captor = ArgumentCaptor.forClass(OrderPaymentResultMessage.class);
        verify(orderPaymentResultEventPublisher).publish(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(OrderPaymentResultStatus.SUCCESS);
        assertThat(captor.getValue().escrowId()).isEqualTo(escrowId);
        assertThat(captor.getValue().buyerWalletId()).isEqualTo(walletId);
    }

    @Test
    @DisplayName("wallet 없음 실패는 FAILED 결과 이벤트를 발행한다")
    void listen_walletNotFound_publishesFailedResult() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID buyerMemberId = UUID.randomUUID();
        UUID sellerMemberId = UUID.randomUUID();
        OrderPaymentRequestedMessage event = new OrderPaymentRequestedMessage(
                "evt-1",
                orderId,
                buyerMemberId,
                sellerMemberId,
                12_000L,
                10_000L,
                LocalDateTime.of(2024, 1, 3, 10, 0, 0)
        );
        String eventJson = "{\"eventId\":\"evt-1\"}";
        given(objectMapper.readValue(eventJson, OrderPaymentRequestedMessage.class)).willReturn(event);

        doThrow(new WalletNotFoundException()).when(orderPaymentUseCase)
                .payOrder(new OrderPaymentCommand(orderId, buyerMemberId, sellerMemberId, 12_000L, 10_000L, null));

        consumer.listen(eventJson);

        ArgumentCaptor<OrderPaymentResultMessage> captor = ArgumentCaptor.forClass(OrderPaymentResultMessage.class);
        verify(orderPaymentResultEventPublisher).publish(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(OrderPaymentResultStatus.FAILED);
        assertThat(captor.getValue().failureReason()).isEqualTo(OrderPaymentFailureReason.WALLET_NOT_FOUND);
    }

    @Test
    @DisplayName("buyerMemberId가 없으면 예외가 발생한다")
    void listen_missingBuyerMemberId_throwsException() throws Exception {
        OrderPaymentRequestedMessage event = new OrderPaymentRequestedMessage(
                "evt-1",
                UUID.randomUUID(),
                null,
                UUID.randomUUID(),
                12_000L,
                10_000L,
                LocalDateTime.of(2024, 1, 3, 10, 0, 0)
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
