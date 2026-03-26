package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.dto.OrderPaymentResult;
import com.example.payment.application.dto.OrderPaymentCommand;
import com.example.payment.application.usecase.OrderPaymentUseCase;
import com.example.payment.domain.exception.InvalidOrderPaymentRequestException;
import com.example.payment.domain.exception.OrderPaymentAlreadyCompletedException;
import com.example.payment.domain.exception.WalletNotFoundException;
import com.example.payment.domain.service.OrderPaymentResultEventPublisher;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentFailureReason;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentRequestedMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentResultMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentResultStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderPaymentRequestedEventConsumer 테스트")
class OrderPaymentRequestedEventConsumerTest {

    @Mock
    private OrderPaymentUseCase orderPaymentUseCase;

    @Mock
    private OrderPaymentResultEventPublisher orderPaymentResultEventPublisher;

    @InjectMocks
    private OrderPaymentRequestedEventConsumer consumer;

    @Test
    @DisplayName("정상 이벤트를 수신하면 주문 결제 유스케이스를 호출한다")
    void listen_validEvent_callsOrderPaymentUseCase() {
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

        given(orderPaymentUseCase.payOrder(any(OrderPaymentCommand.class))).willReturn(result);

        consumer.listen(event);

        ArgumentCaptor<OrderPaymentCommand> captor = ArgumentCaptor.forClass(OrderPaymentCommand.class);
        verify(orderPaymentUseCase).payOrder(captor.capture());
        assertThat(captor.getValue().orderId()).isEqualTo(orderId);
        assertThat(captor.getValue().buyerMemberId()).isEqualTo(buyerMemberId);
        assertThat(captor.getValue().sellerMemberId()).isEqualTo(sellerMemberId);
        assertThat(captor.getValue().orderAmount()).isEqualTo(12_000L);
        assertThat(captor.getValue().sellerReceivableAmount()).isEqualTo(10_000L);
        assertThat(captor.getValue().releaseAt()).isNull();
        verify(orderPaymentResultEventPublisher).publish(any(OrderPaymentResultMessage.class));
    }

    @Test
    @DisplayName("중복 결제 요청 이벤트는 무시한다")
    void listen_duplicateEvent_ignoresAlreadyCompletedException() {
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

        doThrow(new OrderPaymentAlreadyCompletedException()).when(orderPaymentUseCase)
                .payOrder(new OrderPaymentCommand(orderId, buyerMemberId, sellerMemberId, 12_000L, 10_000L, null));

        consumer.listen(event);

        verify(orderPaymentUseCase)
                .payOrder(new OrderPaymentCommand(orderId, buyerMemberId, sellerMemberId, 12_000L, 10_000L, null));
        verify(orderPaymentResultEventPublisher).publish(any(OrderPaymentResultMessage.class));
    }

    @Test
    @DisplayName("wallet 없음 실패 시 FAILED 결과 이벤트를 발행한다")
    void listen_walletNotFound_publishesFailedResult() {
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

        doThrow(new WalletNotFoundException()).when(orderPaymentUseCase)
                .payOrder(new OrderPaymentCommand(orderId, buyerMemberId, sellerMemberId, 12_000L, 10_000L, null));

        consumer.listen(event);

        ArgumentCaptor<OrderPaymentResultMessage> captor = ArgumentCaptor.forClass(OrderPaymentResultMessage.class);
        verify(orderPaymentResultEventPublisher).publish(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(OrderPaymentResultStatus.FAILED);
        assertThat(captor.getValue().failureReason()).isEqualTo(OrderPaymentFailureReason.WALLET_NOT_FOUND);
    }

    @Test
    @DisplayName("buyerMemberId가 없으면 예외가 발생한다")
    void listen_missingBuyerMemberId_throwsException() {
        OrderPaymentRequestedMessage event = new OrderPaymentRequestedMessage(
                "evt-1",
                UUID.randomUUID(),
                null,
                UUID.randomUUID(),
                12_000L,
                10_000L,
                LocalDateTime.of(2024, 1, 3, 10, 0, 0)
        );

        assertThatThrownBy(() -> consumer.listen(event))
                .isInstanceOf(InvalidOrderPaymentRequestException.class)
                .hasMessageContaining("buyerMemberId is required.");

        verifyNoInteractions(orderPaymentUseCase);
    }
}
