package com.example.settlement.application.dto;

import java.time.LocalDateTime;

public record MonthlySettlementAggregateCommand(
        int settlementYear,
        int settlementMonth,
        LocalDateTime releasedAtFrom,
        LocalDateTime releasedAtTo
) {
}
