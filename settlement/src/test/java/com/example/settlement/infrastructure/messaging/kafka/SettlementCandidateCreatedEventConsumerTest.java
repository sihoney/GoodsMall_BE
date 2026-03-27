package com.example.settlement.infrastructure.messaging.kafka;

import static org.mockito.Mockito.verify;

import com.example.settlement.application.dto.SettlementItemCreateCommand;
import com.example.settlement.application.service.MonthlySettlementService;
import com.example.settlement.infrastructure.messaging.kafka.contract.SettlementCandidateCreatedMessage;
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
@DisplayName("SettlementCandidateCreatedEventConsumer 테스트")
class SettlementCandidateCreatedEventConsumerTest {

    @Mock
    private MonthlySettlementService monthlySettlementService;

    @InjectMocks
    private SettlementCandidateCreatedEventConsumer consumer;

    @Test
    @DisplayName("정산 원천 이벤트를 SettlementItemCreateCommand로 변환한다")
    void listen_registersSettlementItem() {
        SettlementCandidateCreatedMessage event = new SettlementCandidateCreatedMessage(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                10_000L,
                LocalDateTime.of(2024, 1, 1, 12, 0),
                "MANUAL",
                LocalDateTime.of(2024, 1, 1, 12, 0, 1)
        );

        consumer.listen(event);

        ArgumentCaptor<SettlementItemCreateCommand> captor = ArgumentCaptor.forClass(SettlementItemCreateCommand.class);
        verify(monthlySettlementService).registerSettlementItem(captor.capture());
    }
}
