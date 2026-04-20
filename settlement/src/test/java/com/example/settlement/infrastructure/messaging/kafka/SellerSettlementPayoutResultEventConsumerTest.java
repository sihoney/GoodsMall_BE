package com.example.settlement.infrastructure.messaging.kafka;

import static org.mockito.Mockito.verify;

import com.example.settlement.application.usecase.SettlementPayoutUseCase;
import com.example.settlement.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultMessage;
import com.example.settlement.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("SellerSettlementPayoutResultEventConsumer 테스트")
class SellerSettlementPayoutResultEventConsumerTest {

    @Mock
    private SettlementPayoutUseCase settlementPayoutService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SellerSettlementPayoutResultEventConsumer consumer;

    @Test
    @DisplayName("지급 결과 이벤트를 SettlementPayoutService로 전달한다")
    void listen_forwardsEventToService() throws Exception {
        SellerSettlementPayoutResultMessage event = new SellerSettlementPayoutResultMessage(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.valueOf(9_000L),
                SellerSettlementPayoutResultStatus.SUCCESS,
                null,
                LocalDateTime.of(2026, 4, 1, 3, 11)
        );
        String eventJson = "{\"eventId\":\"evt-1\"}";
        given(objectMapper.readValue(eventJson, SellerSettlementPayoutResultMessage.class)).willReturn(event);

        consumer.listen(eventJson);

        verify(settlementPayoutService).applyPayoutResult(event);
    }
}

