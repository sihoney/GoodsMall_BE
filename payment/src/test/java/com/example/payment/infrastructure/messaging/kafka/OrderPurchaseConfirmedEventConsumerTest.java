package com.example.payment.infrastructure.messaging.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.payment.application.dto.EscrowReleaseCommand;
import com.example.payment.application.usecase.EscrowReleaseUseCase;
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
    @DisplayName("구매확정 이벤트를 수신하면 escrow release usecase를 호출한다")
    void listen_purchaseConfirmed_callsEscrowReleaseUseCase() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID sellerMemberId = UUID.randomUUID();
        OrderPurchaseConfirmedMessage event = new OrderPurchaseConfirmedMessage(
                "evt-1",
                orderId,
                sellerMemberId,
                Instant.parse("2024-01-03T10:00:00Z"),
                ConfirmationType.AUTO
        );
        String eventJson = "{\"eventId\":\"evt-1\"}";
        given(objectMapper.readValue(eventJson, OrderPurchaseConfirmedMessage.class)).willReturn(event);

        consumer.listen(eventJson);

        ArgumentCaptor<EscrowReleaseCommand> captor = ArgumentCaptor.forClass(EscrowReleaseCommand.class);
        verify(escrowReleaseUseCase).releaseEscrow(captor.capture());
        assertThat(captor.getValue().orderId()).isEqualTo(orderId);
        assertThat(captor.getValue().sellerMemberId()).isEqualTo(sellerMemberId);
        assertThat(captor.getValue().confirmationType()).isEqualTo(ConfirmationType.AUTO);
    }
}
