package com.example.payment.infrastructure.messaging.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.example.payment.application.dto.EscrowReleaseScheduleCommand;
import com.example.payment.application.usecase.EscrowReleaseScheduleUseCase;
import com.example.payment.common.exception.InvalidOrderPaymentRequestException;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderDeliveryCompletedMessage;
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
@DisplayName("OrderDeliveryCompletedEventConsumer 테스트")
class OrderDeliveryCompletedEventConsumerTest {

    @Mock
    private EscrowReleaseScheduleUseCase escrowReleaseScheduleUseCase;

    @InjectMocks
    private OrderDeliveryCompletedEventConsumer consumer;

    @Test
    @DisplayName("정상 배송 완료 이벤트를 수신하면 releaseAt 설정 usecase를 호출한다")
    void listen_validEvent_callsEscrowReleaseScheduleUseCase() {
        UUID orderId = UUID.randomUUID();
        LocalDateTime deliveredAt = LocalDateTime.of(2024, 1, 5, 12, 0, 0);
        OrderDeliveryCompletedMessage event = new OrderDeliveryCompletedMessage(
                "evt-1",
                orderId,
                deliveredAt,
                LocalDateTime.of(2024, 1, 5, 12, 1, 0)
        );

        consumer.listen(event);

        ArgumentCaptor<EscrowReleaseScheduleCommand> captor =
                ArgumentCaptor.forClass(EscrowReleaseScheduleCommand.class);
        verify(escrowReleaseScheduleUseCase).scheduleRelease(captor.capture());
        assertThat(captor.getValue().orderId()).isEqualTo(orderId);
        assertThat(captor.getValue().deliveredAt()).isEqualTo(deliveredAt);
    }

    @Test
    @DisplayName("중복 배송 완료 이벤트도 그대로 usecase에 위임한다")
    void listen_duplicateEvent_callsUseCase() {
        UUID orderId = UUID.randomUUID();
        LocalDateTime deliveredAt = LocalDateTime.of(2024, 1, 5, 12, 0, 0);
        OrderDeliveryCompletedMessage event = new OrderDeliveryCompletedMessage(
                "evt-1",
                orderId,
                deliveredAt,
                LocalDateTime.of(2024, 1, 5, 12, 1, 0)
        );

        consumer.listen(event);

        verify(escrowReleaseScheduleUseCase)
                .scheduleRelease(new EscrowReleaseScheduleCommand(orderId, deliveredAt));
    }

    @Test
    @DisplayName("deliveredAt가 없으면 예외가 발생한다")
    void listen_missingDeliveredAt_throwsException() {
        OrderDeliveryCompletedMessage event = new OrderDeliveryCompletedMessage(
                "evt-1",
                UUID.randomUUID(),
                null,
                LocalDateTime.of(2024, 1, 5, 12, 1, 0)
        );

        assertThatThrownBy(() -> consumer.listen(event))
                .isInstanceOf(InvalidOrderPaymentRequestException.class)
                .hasMessageContaining("deliveredAt is required.");

        verifyNoInteractions(escrowReleaseScheduleUseCase);
    }
}
