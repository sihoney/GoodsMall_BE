package com.example.settlement.infrastructure.messaging.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import com.example.settlement.application.dto.SettlementItemCreateCommand;
import com.example.settlement.application.usecase.MonthlySettlementUseCase;
import com.example.settlement.infrastructure.messaging.kafka.contract.SettlementCandidateCreatedMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.math.BigDecimal;
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
@DisplayName("SettlementCandidateCreatedEventConsumer 테스트")
class SettlementCandidateCreatedEventConsumerTest {

    @Mock
    private MonthlySettlementUseCase monthlySettlementService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SettlementCandidateCreatedEventConsumer consumer;

    @Test
    @DisplayName("정산 원천 이벤트를 SettlementItemCreateCommand로 변환한다")
    void listen_registersSettlementItem() throws Exception {
        SettlementCandidateCreatedMessage event = new SettlementCandidateCreatedMessage(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.valueOf(10_000L),
                Instant.parse("2024-01-01T12:00:00Z"),
                "MANUAL",
                Instant.parse("2024-01-01T12:00:01Z")
        );
        String eventJson = "{\"eventId\":\"evt-1\"}";
        given(objectMapper.readValue(eventJson, SettlementCandidateCreatedMessage.class)).willReturn(event);

        consumer.listen(eventJson);

        ArgumentCaptor<SettlementItemCreateCommand> captor = ArgumentCaptor.forClass(SettlementItemCreateCommand.class);
        verify(monthlySettlementService).registerSettlementItem(captor.capture());
        assertThat(captor.getValue().orderId()).isEqualTo(event.orderId());
        assertThat(captor.getValue().escrowId()).isEqualTo(event.escrowId());
        assertThat(captor.getValue().sellerId()).isEqualTo(event.sellerMemberId());
        assertThat(captor.getValue().grossAmount()).isEqualTo(event.grossAmount());
        assertThat(captor.getValue().releasedAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 21, 0));
    }

    @Test
    @DisplayName("escrowId가 없으면 예외가 발생한다")
    void listen_withoutEscrowId_throwsException() throws Exception {
        SettlementCandidateCreatedMessage event = new SettlementCandidateCreatedMessage(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                UUID.randomUUID(),
                BigDecimal.valueOf(10_000L),
                Instant.parse("2024-01-01T12:00:00Z"),
                "MANUAL",
                Instant.parse("2024-01-01T12:00:01Z")
        );
        String eventJson = "{\"eventId\":\"evt-1\"}";
        given(objectMapper.readValue(eventJson, SettlementCandidateCreatedMessage.class)).willReturn(event);

        assertThatThrownBy(() -> consumer.listen(eventJson))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Failed to deserialize SettlementCandidateCreatedMessage");
    }
}
