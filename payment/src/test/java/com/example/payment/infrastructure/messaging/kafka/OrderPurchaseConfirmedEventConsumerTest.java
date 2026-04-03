package com.example.payment.infrastructure.messaging.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.example.payment.application.dto.EscrowReleaseCommand;
import com.example.payment.application.usecase.EscrowReleaseUseCase;
import com.example.payment.common.exception.InvalidOrderPaymentRequestException;
import com.example.payment.domain.enumtype.ConfirmationType;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPurchaseConfirmedMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderPurchaseConfirmedEventConsumer 테스트")
class OrderPurchaseConfirmedEventConsumerTest {

    @Mock
    private EscrowReleaseUseCase escrowReleaseUseCase;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OrderPurchaseConfirmedEventConsumer consumer;

    @Test
    @DisplayName("MANUAL 구매확정 이벤트를 수신하면 escrow 해제 usecase를 호출한다")
    void listen_manualEvent_callsEscrowReleaseUseCase() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID sellerMemberId = UUID.randomUUID();
        OrderPurchaseConfirmedMessage event = new OrderPurchaseConfirmedMessage(
                "evt-1",
                orderId,
                sellerMemberId,
                Instant.parse("2024-01-03T10:00:00Z"),
                ConfirmationType.MANUAL
        );
        String eventJson = "{\"eventId\":\"evt-1\"}";
        given(objectMapper.readValue(eventJson, OrderPurchaseConfirmedMessage.class)).willReturn(event);

        consumer.listen(eventJson);

        ArgumentCaptor<EscrowReleaseCommand> captor = ArgumentCaptor.forClass(EscrowReleaseCommand.class);
        verify(escrowReleaseUseCase).releaseEscrow(captor.capture());
        assertThat(captor.getValue().orderId()).isEqualTo(orderId);
        assertThat(captor.getValue().sellerMemberId()).isEqualTo(sellerMemberId);
        assertThat(captor.getValue().confirmationType()).isEqualTo(ConfirmationType.MANUAL);
    }

    @Test
    @DisplayName("AUTO 구매확정 이벤트는 처리하지 않는다")
    void listen_autoEvent_throwsException() throws Exception {
        OrderPurchaseConfirmedMessage event = new OrderPurchaseConfirmedMessage(
                "evt-1",
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.parse("2024-01-03T10:00:00Z"),
                ConfirmationType.AUTO
        );
        String eventJson = "{\"eventId\":\"evt-1\"}";
        given(objectMapper.readValue(eventJson, OrderPurchaseConfirmedMessage.class)).willReturn(event);

        assertThatThrownBy(() -> consumer.listen(eventJson))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(InvalidOrderPaymentRequestException.class)
                .hasMessageContaining("Failed to deserialize OrderPurchaseConfirmedMessage");

        verifyNoInteractions(escrowReleaseUseCase);
    }

    @Test
    @DisplayName("중복 수동 구매확정도 그대로 usecase에 위임한다")
    void listen_duplicateManualEvent_callsUseCase() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID sellerMemberId = UUID.randomUUID();
        OrderPurchaseConfirmedMessage event = new OrderPurchaseConfirmedMessage(
                "evt-1",
                orderId,
                sellerMemberId,
                Instant.parse("2024-01-03T10:00:00Z"),
                ConfirmationType.MANUAL
        );
        String eventJson = "{\"eventId\":\"evt-1\"}";
        given(objectMapper.readValue(eventJson, OrderPurchaseConfirmedMessage.class)).willReturn(event);

        consumer.listen(eventJson);

        verify(escrowReleaseUseCase).releaseEscrow(new EscrowReleaseCommand(orderId, sellerMemberId, ConfirmationType.MANUAL));
    }
}
