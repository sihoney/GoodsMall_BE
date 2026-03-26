package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.dto.CreateWalletCommand;
import com.example.payment.application.event.MemberCreatedEvent;
import com.example.payment.application.usecase.CreateWalletUseCase;
import com.example.payment.domain.exception.InvalidChargeRequestException;
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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberCreatedEventConsumer 테스트")
class MemberCreatedEventConsumerTest {

    @Mock
    private CreateWalletUseCase createWalletUseCase;

    @InjectMocks
    private MemberCreatedEventConsumer consumer;

    @Test
    @DisplayName("member created 이벤트를 받으면 wallet 생성 유스케이스를 호출한다")
    void listen_validEvent_callsCreateWalletUseCase() {
        UUID memberId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        MemberCreatedEvent event = new MemberCreatedEvent("evt-1", memberId, occurredAt);

        consumer.listen(event);

        ArgumentCaptor<CreateWalletCommand> captor = ArgumentCaptor.forClass(CreateWalletCommand.class);
        verify(createWalletUseCase).createWallet(captor.capture());
        assertThat(captor.getValue().memberId()).isEqualTo(memberId);
        assertThat(captor.getValue().createdAt()).isEqualTo(occurredAt);
    }

    @Test
    @DisplayName("memberId가 없으면 예외가 발생한다")
    void listen_missingMemberId_throwsException() {
        MemberCreatedEvent event = new MemberCreatedEvent("evt-1", null, LocalDateTime.of(2024, 1, 1, 12, 0, 0));

        assertThatThrownBy(() -> consumer.listen(event))
                .isInstanceOf(InvalidChargeRequestException.class)
                .hasMessageContaining("memberId is required.");
    }
}
