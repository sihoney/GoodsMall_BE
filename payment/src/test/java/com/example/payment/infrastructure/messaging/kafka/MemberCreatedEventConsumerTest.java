package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.dto.CreateWalletCommand;
import com.example.payment.application.usecase.CreateWalletUseCase;
import com.example.payment.common.exception.InvalidChargeRequestException;
import com.example.payment.infrastructure.messaging.kafka.contract.MemberCreatedMessage;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberCreatedEventConsumer 테스트")
class MemberCreatedEventConsumerTest {

    @Mock
    private CreateWalletUseCase createWalletUseCase;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private MemberCreatedEventConsumer consumer;

    @Test
    @DisplayName("member created 이벤트를 받으면 wallet 생성 유스케이스를 호출한다")
    void listen_validEvent_callsCreateWalletUseCase() throws Exception {
        UUID memberId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        MemberCreatedMessage event = new MemberCreatedMessage("evt-1", memberId, occurredAt);
        String eventJson = "{\"eventId\":\"evt-1\"}";
        given(objectMapper.readValue(eventJson, MemberCreatedMessage.class)).willReturn(event);

        consumer.listen(eventJson);

        ArgumentCaptor<CreateWalletCommand> captor = ArgumentCaptor.forClass(CreateWalletCommand.class);
        verify(createWalletUseCase).createWallet(captor.capture());
        assertThat(captor.getValue().memberId()).isEqualTo(memberId);
        assertThat(captor.getValue().createdAt()).isEqualTo(occurredAt);
    }

    @Test
    @DisplayName("memberId가 없으면 예외가 발생한다")
    void listen_missingMemberId_throwsException() throws Exception {
        MemberCreatedMessage event = new MemberCreatedMessage("evt-1", null, LocalDateTime.of(2024, 1, 1, 12, 0, 0));
        String eventJson = "{\"eventId\":\"evt-1\"}";
        given(objectMapper.readValue(eventJson, MemberCreatedMessage.class)).willReturn(event);

        assertThatThrownBy(() -> consumer.listen(eventJson))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(InvalidChargeRequestException.class)
                .hasMessageContaining("Failed to deserialize MemberCreatedMessage");
    }
}
