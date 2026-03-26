package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.dto.EscrowReleaseCommand;
import com.example.payment.application.usecase.EscrowReleaseUseCase;
import com.example.payment.domain.enumtype.ConfirmationType;
import com.example.payment.domain.exception.EscrowAlreadyReleasedException;
import com.example.payment.domain.exception.InvalidOrderPaymentRequestException;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPurchaseConfirmedMessage;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderPurchaseConfirmedEventConsumer 테스트")
class OrderPurchaseConfirmedEventConsumerTest {

    @Mock
    private EscrowReleaseUseCase escrowReleaseUseCase;

    @InjectMocks
    private OrderPurchaseConfirmedEventConsumer consumer;

    @Test
    @DisplayName("MANUAL 구매확정 이벤트 수신 시 escrow 해제 유스케이스를 호출한다")
    void listen_manualEvent_callsEscrowReleaseUseCase() {
        UUID orderId = UUID.randomUUID();
        UUID sellerMemberId = UUID.randomUUID();
        OrderPurchaseConfirmedMessage event = new OrderPurchaseConfirmedMessage(
                "evt-1",
                orderId,
                sellerMemberId,
                LocalDateTime.of(2024, 1, 3, 10, 0, 0),
                ConfirmationType.MANUAL
        );

        consumer.listen(event);

        ArgumentCaptor<EscrowReleaseCommand> captor = ArgumentCaptor.forClass(EscrowReleaseCommand.class);
        verify(escrowReleaseUseCase).releaseEscrow(captor.capture());
        assertThat(captor.getValue().orderId()).isEqualTo(orderId);
        assertThat(captor.getValue().sellerMemberId()).isEqualTo(sellerMemberId);
        assertThat(captor.getValue().confirmationType()).isEqualTo(ConfirmationType.MANUAL);
    }

    @Test
    @DisplayName("AUTO 구매확정 이벤트는 처리하지 않는다")
    void listen_autoEvent_throwsException() {
        OrderPurchaseConfirmedMessage event = new OrderPurchaseConfirmedMessage(
                "evt-1",
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.of(2024, 1, 3, 10, 0, 0),
                ConfirmationType.AUTO
        );

        assertThatThrownBy(() -> consumer.listen(event))
                .isInstanceOf(InvalidOrderPaymentRequestException.class)
                .hasMessageContaining("Only MANUAL confirmation event is allowed.");

        verifyNoInteractions(escrowReleaseUseCase);
    }

    @Test
    @DisplayName("이미 RELEASED 상태로 인한 중복 이벤트는 무시한다")
    void listen_duplicateManualEvent_ignoresAlreadyReleasedException() {
        UUID orderId = UUID.randomUUID();
        UUID sellerMemberId = UUID.randomUUID();
        OrderPurchaseConfirmedMessage event = new OrderPurchaseConfirmedMessage(
                "evt-1",
                orderId,
                sellerMemberId,
                LocalDateTime.of(2024, 1, 3, 10, 0, 0),
                ConfirmationType.MANUAL
        );

        doThrow(new EscrowAlreadyReleasedException()).when(escrowReleaseUseCase)
                .releaseEscrow(new EscrowReleaseCommand(orderId, sellerMemberId, ConfirmationType.MANUAL));

        consumer.listen(event);

        verify(escrowReleaseUseCase).releaseEscrow(new EscrowReleaseCommand(orderId, sellerMemberId, ConfirmationType.MANUAL));
    }
}
